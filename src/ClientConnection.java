import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * ClientConnection manages the ServerController's interactions with the Client.
 * It contains a Socket with which to communicate with clients. One of its
 * Threads listens to the client for messages to pass to the server, while
 * another listens to the server for messages to pass to the client. When
 * a message is received to go to the server, this class calls the appropriate
 * ServerController method(s) and passes on the necessary information.
 * @author Emily Shane
 * @author Ethan Wiederspan
 * 
 * UPDATE 4/23/18(EMW): Sends the players username back to client, most 
 *        important for anonymous usernames.
 */
public class ClientConnection implements Runnable{
	Socket s;
	private InputStream in;
	private OutputStream out;
	private Thread worker;
	private ServerController control;
	private ServerModel model;
	private User user = null;
	private ArrayList<String> invitesSent;
	private ArrayList<String> invitesReceived;
	private String ip;
	private boolean inGame;

	private ClientConnection peer = null;

	public ClientConnection(Socket clientSock, ServerController c, ServerModel m){
		s = clientSock;
		control = c;
		model = m;
		invitesSent = new ArrayList<String>();
		invitesReceived = new ArrayList<String>();
		
		try{
			in = s.getInputStream();
			out = s.getOutputStream();
			ip = s.getInetAddress().getHostAddress();
		}catch(IOException e){
			System.exit(1);
		}
		//in a new Thread, listen for messages from the user
		clientListen();

	}

	/**
	 * Write to the Socket's OutputStream to send new message to the client.
	 * If this throws an error, the ClientConnection is removed from the list of active connections
	 *
	 * @param message the message to send to the client
	 */
	public void toClient(String message){
		// Added newline char to separate messages
		message = message + "\n";
		
		// Write to Socket
		byte [] buff;
		buff = message.getBytes();
		try{
			out.write(buff, 0, message.length());
			out.flush();
		} catch(IOException e){
			//TODO: if writing to the OutputStream gives an error, we should disconnect this client
			control.disconnect(this);
		}
	}

	/**
	 * Instantiates the Thread that will listen to the client for messages to send to the ServerController
	 */
	public void clientListen(){ //this-->ServerController-->ServerModel-->other Clients
		worker = new Thread(this);
		worker.start();
	}

	/**
	 * returns true if there is an assigned User that is online
	 * returns false if there is no assigned User, or if the assigned User is
	 * not online
	 * @return true or false depending on whether there is a User assigned and
	 * whether it is online or not
	 */
	public boolean online(){
		if(user == null)
			return false;
		else
			return user.isOnline();
	}

	/**
	 * Thread that listens to the client for messages to send to the server
	 */
	public void run(){
		byte [] buffer = new byte[1000];
		boolean connected = true;
		connected = (s.isConnected() && !s.isClosed());
		Scanner mScan;
		
		while(connected) {
			try {
				String recMes = null;
				//InputStream gets info from Client's OutputStream
				int msgTemp = in.read(buffer);
				String msg = null;
				if (msgTemp > 0) {
					
					recMes = new String(buffer, 0, msgTemp);
					mScan = new Scanner(recMes);
					while(mScan.hasNext()) {
						processMsg(mScan.nextLine());
					}
					
					
				}
				connected = (s.isConnected() && !s.isClosed());
			}catch (IOException e) {
				connected = (s.isConnected() && !s.isClosed());
			}
		}//while

	}//run


	/**
	 * Called by the Run method of the Thread that listens 
	 * to the Socket, processes the message and transfers 
	 * control based on the tags sent by client.
	 * 
	 * @msg The messages read from the Socket
	 */
	private void processMsg(String msg) {


		//format of authentication message is [TAG]username password
		if(msg.startsWith(Tags.REGISTER)){
			register(msg);
		}else if(msg.startsWith(Tags.LOGIN)){
			login(msg);
		}else if(msg.startsWith(Tags.ANON)){
			anon(msg);                    	
		}else if(msg.startsWith(Tags.DISCONNECT)){
			control.disconnect(this);
		}else if(msg.startsWith(Tags.INVITE)){
			sendInvite(msg);
		}else if(msg.startsWith(Tags.RESPOND)) {
			sendResponse(msg);
		}else if(msg.startsWith(Tags.CANCELINVITE)) {
			this.model.cancelRequest(this, msg);
		}else if(msg.startsWith(Tags.HOST)) {
			peer.toClient(msg + this.ip);
		}else if(msg.startsWith(Tags.GAME_FEEDBACK)){
			gameOver(msg.substring(Tags.GAME_FEEDBACK.length(),msg.length()));
		}
		else{
			toClient("Error in authentication");
		}


	}// processMsg

	/**
	 * When a [REG] tag is encountered by the second Thread, this method is
	 * called to format the inputs for--and call--the ServerController's 
	 * register method
	 * @param recMes the message from the client side
	 */
	private void register(String recMes){
		
		//TODO remove constants
		int space = recMes.indexOf(' ');
		String userName = recMes.substring(Tags.REGISTER.length(),space);
		String pass = recMes.substring(space+1,recMes.length());

		//the ServerController register method returns a String message
		String servMsg = control.register(userName, pass, this);

		if(servMsg.equals(Tags.SUCCESS)){
			user.setOnline(true); //success
			toClient(Tags.AUTH_FEEDBACK + Tags.SUCCESS + this.getUser().getUN()); //tell client it was a success
			
			//sleep the thread to keep the messages from overlapping, then go to matchmaking
			try{Thread.sleep(1000); }catch(InterruptedException e){e.printStackTrace();}
			this.toMatchmaking();
		}
		else{
			//username already taken
			toClient(Tags.AUTH_FEEDBACK + Tags.UN_TAKEN); //failure
		}
	}//register

	/**
	 * When a [LOGIN] tag is encountered by the second Thread, this method is
	 * called to format the inputs for--and call--the ServerController's 
	 * authenticate method
	 * @param recMes the message from the client side
	 */
	private void login(String recMes){
		
		int space = recMes.indexOf(' ');
		String userName = recMes.substring(Tags.LOGIN.length(),space);
		String pass = recMes.substring(space+1,recMes.length());

		//ServerController's authenticate method returns a String
		String servMsg = control.authenticate(userName, pass, this);
		
		if(servMsg.equals(Tags.SUCCESS)){
			user.setOnline(true); //success
			toClient((Tags.AUTH_FEEDBACK + Tags.SUCCESS + this.getUser().getUN()));
			
			//sleep the thread to keep the messages from overlapping
			try{Thread.sleep(100); }catch(InterruptedException e){e.printStackTrace();}
			this.toMatchmaking();
		}else if(servMsg.equals(Tags.UN_NOT_FOUND )){
			//username not found
			toClient( (Tags.AUTH_FEEDBACK + Tags.UN_NOT_FOUND) ); //failure 1
		}else if(servMsg.equals(Tags.UPDATEONLINE)){
			//player is already online
			toClient( (Tags.AUTH_FEEDBACK + Tags.UPDATEONLINE) ); //failure 2
		}else if(servMsg.equals(Tags.WRONG_PASS)) {
			//wrong password
			toClient( (Tags.AUTH_FEEDBACK + Tags.UPDATEONLINE) ); //failure 3
		}
	}

	/**
	 * When a Tags.ANON is encountered by the second Thread, this method is
	 * called to format the inputs for--and call--the ServerController's 
	 * newAnon method
	 * 
	 * @param recMes the message from the client side
	 */
	private void anon(String recMes){
		toClient(control.newAnon(this));
		//send the success message directly to the client
		user.setOnline(true);
		//sleep the thread to keep the messages from overlapping, then goto matchmaking
		try{Thread.sleep(100); }catch(InterruptedException e){e.printStackTrace();}
		this.toMatchmaking();
	}
	
	/**
	 * Brings this ClientConnection to the next GameState, Sends 
	 * this connection a list on online users and lets the other 
	 * users know that this player is online
	 * 
	 */
	public void toMatchmaking() {
		//this.user.setGameState(2);
		
		this.model.whoseOnline(this);
		this.model.broadcastOnline(this);
	}

	/**
	 * When Tags.INVITE is encounted by the Socket Listener, this method is 
	 * called to send an invite to the specified player.
	 * 
	 * @param receiver the data containing the message type and receiver's username
	 * receiver = "[Tag for invite]receiever's username"
	 */
	private void sendInvite(String receiver) {
		receiver = receiver.substring((Tags.INVITE.length()), receiver.length() );
		
		invitesSent.add(receiver);
		control.challenge(this, receiver);
	}

	/**
	 * Receives an invite to a game.  Called by Server 
	 * controller when an Invite is sent to this Connection 
	 * from another player.
	 * 
	 * @param senderUN the username of the player who sent the Invite to this connection
	 */
	public void receiveInvite(String senderUN) {
		invitesReceived.add(senderUN);
		this.toClient( (Tags.MATCH_FEEDBACK + Tags.INVITE  + senderUN) );
	}


	/**
	 * Sends a challenge-response to the player that challenged them. Takes 
	 * in all of the needed info in a single string received through the Socket.  
	 * Called when TAG.RESPOND is encountered by the Socket Listener.
	 * 
	 * @param response The string containing all of the data for a player's 
	 * resonse to a challenge
	 * 
	 * response = [tag for a response][tag for conf/deny]username of receiver
	 */
	private void sendResponse(String response) {
		
		//find username of receiver
		response = response.substring(Tags.RESPOND.length(), response.length());
		String targetUN = response.substring( (Tags.CONFIRM.length()), response.length());  
		String answer = response.substring(0, Tags.CONFIRM.length());
		
		//if yes, set as peer
		if(answer.contains(Tags.CONFIRM)) {
			peer = model.lookUp(targetUN);
		}
		control.respondToChallenge(this, targetUN , answer);
		

	}

	/**
	 * Receivers a response to a challenge.  Called by the server 
	 * controller when a response is sent by another player
	 * 
	 * @param sender the username of the player who sent the response
	 * @param response the response from that player
	 */
	public void receiveResponse(String sender, String response) {

		this.toClient( (Tags.MATCH_FEEDBACK + Tags.RESPOND + response + sender));
	}

	/**
	 * Handles the game over message
	 *
	 * @param msg
	 */
	public void gameOver(String msg){
			int comma = msg.indexOf(',');
			String winner = msg.substring(0,comma);
			String loser = msg.substring(comma+1,msg.length());

			if(this.user.getUN().equals(winner)){
				this.user.incrementWins();
			}

			//TODO: use loser to implement point system
	}
	

	/**
	 * Getter and Setter methods for this ClientConnection's User
	 */
	public User getUser() {
		return this.user;
	}
	public void setUser(User u){
		this.user = u;
	}

	/**
	 * Getter and Setter methods for this ClientConnection's peer
	 */
	public ClientConnection getPeer() {
		return peer;
	}

	public void setPeer(ClientConnection peer) {
		this.peer = peer;
	}
	
	/**
	 * returns whether this user is currently playing a game
	 * @return true if user is in game and false otherwise
	 */
	public boolean isInGame(){
		return inGame;
	}

	/**
	 * setter method for inGame field (called by TODO:)
	 */
	public void setInGame(boolean tf){
		inGame = tf;
	}

}

