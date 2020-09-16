import java.io.*;
import java.net.*;
import java.util.Scanner;
/**
 * Object that hold master control of the processes of the client 
 * side of Gomoku.  All communication with the Server must go through 
 * the Master Controller.  Creates and disposes of the various 
 * controllers and GUIs for the different stages of the program.
 * @author Natalie Stephenson
 * 
 * Updated prior to 4/20/18 to include functionality for Matchmaking
 * and to implement use of the Tags class.
 * @author Emily Shane
 * @author Ethan Wiederspan
 * 
 */
public class MasterClientController implements Runnable{
	private Thread worker;
	private Socket sock;
	private OutputStream out;
	private InputStream in;
	private DataOutputStream dout;
	
	private boolean inGame;
	private String gameType;
	private String username;
	private String password;
	private LoginController logCon;
	private MatchmakingController matchCon;
	private PlayGameController gameCon;
	
	private String server;
	private int servPort;
	private boolean connected = false;
	
	public MasterClientController( String[] args) {
		logCon = new LoginController( this );
		
	}
	
	/**
	 * Connects to the Sever
	 */
	public void connectToServer(String srvr, int port) {
		server = srvr;
		servPort = port;
		try {
			sock = new Socket( server, servPort );
			
			// IO Streams:
			out = sock.getOutputStream();
			in =  sock.getInputStream();
			dout = new DataOutputStream( out );
			this.startListening();
			connected = true;
		}catch(IOException e) {
			logCon.updateMessage("Could not connect to the server");
		}
		
	}
	
	/*
	 * Sends Message to server
	 */
	public boolean sendMessage( String msg ) {
		String message = msg+"\n";
		
		byte[] buff;
		buff = message.getBytes();
		
		try {
			dout.write(buff, 0, message.length() );
			dout.flush();
			return true;
		} catch ( Exception e ) {
			return false;
		}
	}
	
	/*
	 * Starts a second thread to receive messages from Server
	 * To be called in Main method
	 */
	public void startListening() {
		worker = new Thread(this);
		worker.start();
	}
	
	/*
	 * Receives message from server
	 * If the message starts with the tag [AUTH] - controller handles tag accordingly and sends
	 * the message further onto the correct controller for the current view 
	 */
	public void run() {
		byte [] buffer = new byte[1000];
		Scanner mScan;
		
		
		while( true ) {
			try {
				String fullMessage = "";
				String msg = "";
				int msgTemp = in.read(buffer);
				if( msgTemp > 0 ) {
					fullMessage = new String(buffer, 0, msgTemp);
					
					mScan = new Scanner(fullMessage);
					while(mScan.hasNext()) {
						msg = mScan.nextLine();
						if( msg.startsWith(Tags.AUTH_FEEDBACK) ) {
							logCon.processMessage(msg.substring(Tags.AUTH_FEEDBACK.length(), msg.length()));
						}
						else if(msg.startsWith(Tags.MATCH_FEEDBACK))
							matchCon.processMessage(msg.substring(Tags.MATCH_FEEDBACK.length(), msg.length()));
						else if(msg.startsWith(Tags.HOST))
							this.joinGame(msg.substring(Tags.HOST.length(), msg.length()));
					}
				}
				
			} catch (Exception e) {
				System.out.println( "Error receiving messages: " + e.toString());
			}
		}
	}

	/**
	 * Closes GUI and Controller for Login and enters the 
	 * Match Making sequence 
	 */
	public void toMatchMaking() {
		
		// Dereference the Login Controller
		logCon = null;
		
		// Make the Match Making Controller
		matchCon = new MatchmakingController(this);
	}
	
	/**
	* called when invite has been accepted and this Controller needs
	* to connect to the new serversocket via the gameCon
	*/
	public void joinGame(String info){
		int gamePort = Integer.parseInt(info.substring(0,4));
		String gameIP = info.substring(4,info.length());
		gameCon = new PlayGameController("opponent",gameIP,gamePort);
		gameCon.setMaster(this);
	}
	
	/**
	 * called when accepting an invite
	 */
	public void hostGame(int port) {
		
		// Make Player1, a local controller
		gameCon = new PlayGameController("opponent");
		
		// Make Player2, the local representation of the remote player
		// This will act as the Server that the Client player will connect to
		
		PlayGameController player2 = new PlayGameController("opponent", port);
		this.sendMessage(Tags.HOST+port);
		
		// took out loop
		
		System.out.println("Both players connected, starting game");
		
		gameCon.setMaster(this);
		player2.setMaster(this);
		
		// Make the Game Model
		GameModel board = new GameModel(gameCon, player2);	
		board.startGame();
	}
	
	public void offlineGame(String difficulty) {
		// Make Player1, a local controller
		gameCon = new PlayGameController("opponent");
		PlayGameController player2 = new PlayGameController("opponent", difficulty);
		
		System.out.println("Both players connected, starting game");
		
		gameCon.setMaster(this);
		player2.setMaster(this);
		
		// Make the Game Model
		GameModel board = new GameModel(gameCon, player2);	
		board.startGame();
		
	}

	/*
	 * Getter and Setter for the selected gameType
	 */
	public String getGameType() {
		return gameType;
	}
	public void setGameType( String gt ) {
		gameType = gt;
	}
	
	/*
	 * Getter and setter for Users username
	 */
	public String getUsername() {
		return username;
	}
	public void setUsername( String un ) {
		username = un;
	}
	
	/*
	 * getter and setter for Users password
	 */
	/*public String getPassword() {
		return password;
	}*/
	public void setPassword( String ps ) {
		password = ps;
	}
	
	/**
	 * getter and setter for connection status
	 */
	public boolean isConnected() {
		return connected;
	}
	public void setConnection(boolean c) {
		connected = c;
	}

	public void disconnect(int status){

		System.out.println("in masterCon.disconnect with status: "+status);

		System.out.println("gameCon: "+gameCon + "  \nMatchCon:" + matchCon);
		
		if(status==1) {// from Matchmaking
			this.sendMessage(Tags.DISCONNECT);
			System.exit(0);
		}if(status==2) {
			// the View will be disposed and only matchmaking will remain. 
		}
		
	}

}
