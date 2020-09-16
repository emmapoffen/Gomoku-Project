import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * ServerController deals with commands from a ClientConnection and provides
 * information from the ServerModel to any ClientConnection as needed.
 * @author EmilyShane
 * @author EthanWiederspan
 * 
 * UPDATE 4/23/18 (EMW): Moved newAnon method to ServerModel
 *
 */
public class ServerController implements Runnable {
	private ServerModel servModel;
	private ServerSocket ss;
	private Thread worker;
	private int anonIndex;
	private boolean run;

	/**
	 * Constructor for ServerController
	 * @param m the ServerModel to assign to this ServerController
	 */
	public ServerController(ServerModel m){

		servModel = m;
		try {
			ss = new ServerSocket(m.getPortNumber());
		}catch(IOException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		anonIndex = 0;

	}

	/**
	 * The method to start the second Thread, which listens for new connections
	 */
	public void startListening(){

		worker = new Thread(this);
		worker.start();
	}


	/**
	 * Sends message to output stream (to the specified client)
	 * @param client the client to whom the message should be sent
	 * @param msg the message to send to the specified client
	 */
	public void sendToUser(ClientConnection client, String msg){
		client.toClient(msg);
	}

	/**
	 * Thread that waits for connections
	 */
	public void run(){
		run = true;
		while(run) {
			try {
				Socket clientSock = ss.accept();
				String info = "Client at " + clientSock.getInetAddress().getHostAddress() + " on port " +
						clientSock.getPort();
				servModel.newConnection(new ClientConnection(clientSock, this,  this.servModel),info);
			} catch (IOException e) {
				System.out.println("Error accepting connection");
			}
		}
	}

	/**
	 * Called by the ClientConnection. Calls the ServerModel's authenticate
	 * @return the String message that the ServerModel returns to tell the 
	 * user the result of their login attempt
	 */
	public String authenticate(String user, String pass, ClientConnection con){
		return servModel.authenticate(user, pass, con);
	}

	/**
	 * Closes the ServerSocket when the server is stopped
	 */
	public void stopServer(){
		run = false;
		try {
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

	/**
	 * Called by the ClientConnection. Calls the ServerModel's register
	 * @return the String message that the ServerModel returns to tell the 
	 * user the result of their registration attempt
	 */
	public String register(String user, String pass, ClientConnection con){
		return servModel.register(user, pass, con);
	}

	/**
	 * Called by the ClientConnection. Calls the ServerModel's newAnon
	 * @return the String message that the ServerModel returns to tell the 
	 * user the result of their anonymous login attempt
	 */
	public String newAnon(ClientConnection con){
		return servModel.newAnon(con);
	}

	/**
	 * Called by a ClientConnection when it needs to disconnect from the server
	 * Calls the ServerModel's disconnect
	 * 
	 * @return
	 */
	public void disconnect(ClientConnection cliCon) {
		servModel.disconnect(cliCon);
	}

	/**
	 * Called by a ClientConnection when they want to issue 
	 * a challenge to another player.  Attempts to find the 
	 * other player and calls the receive function of their 
	 * ClientConnection.
	 * 
	 * @param sender the ClientConnection of the player who sent the invite
	 * @param receiver a String containing the username of the target player
	 * @return a String tag based on the success of the operation
	 */
	public String challenge(ClientConnection sender, String receiver) {

		// Find the receiver
		ClientConnection receiverConn = servModel.lookUp(receiver);

		// Return usernameNotFound if unable to find user
		if(receiverConn == null) {
			return Tags.UN_NOT_FOUND;
		}
		
		// Call receive function of the receiver
		receiverConn.receiveInvite(sender.getUser().getUN());

		return Tags.SUCCESS;
	}

	/**
	 * Called by a ClientConnection when they want to respond
	 * to a challenge to another player.  Attempts to find the 
	 * other player and calls the receive function of their 
	 * ClientConnection.
	 * 
	 * @param sender the ClientConnection of the player responding to the challenge
	 * @param receiver a String containing the username of the player that originally sent the challenge
	 * @param answer the player's response to the challenge, a tag containing either [CONF] or [DENY]
	 * @return a String tag based on the success of the operation
	 */
	public String respondToChallenge(ClientConnection sender, String receiver, String answer) {

		// Find the receiver
		ClientConnection receiverConn = servModel.lookUp(receiver);

		// Return usernameNotFound if unable to find user
		if(receiverConn == null) {
			return Tags.UN_NOT_FOUND;
		}

		ClientConnection target = servModel.lookUp(receiver);
		if(answer.contains(Tags.CONFIRM)) {
			if (!target.isInGame()) {
				receiverConn.receiveResponse(sender.getUser().getUN(), answer);
			}
			else{
				sender.toClient(Tags.MATCH_FEEDBACK + Tags.FAIL + receiver);
			}
		}
		else{
			receiverConn.receiveResponse(sender.getUser().getUN(), answer);
		}
		// Call receive function of the receiver
		return Tags.SUCCESS;
	}


}
