import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket; 
import java.net.Socket;
import java.util.Scanner;

import javax.swing.plaf.OptionPaneUI; 

/**
 * @author NatalieStephenson
 * @author EthanWiederspan
 * Controller for the GamePlay of the Gomoku game. Acts as three 
 * versions of the same Class. One version represents the local 
 * player on the Host's machine. It has reference to the Host 
 * player's View and the Game Model. The second is a local 
 * representation of the client player, it has reference to the 
 * client player's view and the Socket to send data to the client 
 * player.  The Last version is the remote representation of the 
 * client player. This version exists on the host side and receives 
 * data through the Socket and sends it to the Game Model.
 *
 */
public class PlayGameController implements Runnable{

	private ServerSocket servSock; 
	private Socket sock; 
	private Thread clientThread;
	static final int SSPORT = 5432; 
	
	private InputStream in;
	private OutputStream out;
	
	private MasterClientController masterCon;
	private PlayGameView gameView;
	private GameModel gameModel;
	private Gomobot bot;
	
	private int pieceColor; // 1 for white, 2 for black
	
	private boolean connected;
	private Thread worker;
	private String opponent;
	private boolean victory = false;


	
	/**
	 * Contructor for a local player, gets inputs from the 
	 * local player through the PlayGameView, Sends move to 
	 * the PlayGameModel which is on this local machine, thus 
	 * no socket communication is needed.
	 * 
	 * @param opp the Username of your opponent
	 */
	public PlayGameController(String opp){
		System.out.println("Making Local Player 1");
		
		opponent = opp;
		pieceColor = 2;

		gameView = new PlayGameView( this );
	}
	
	/**
	 * Constructor for an AI player, gets input from a Gomobot 
	 * Object, the AI that chooses a move for the computer. 
	 * 
	 * @param opp the username of your opponent
	 * @param b a Gomobot of any difficulty
	 */
	public PlayGameController(String opp, String diff) {
		System.out.println("Making AI Player 2");
		
		pieceColor = 1;
		bot = new Gomobot(this, diff); 
		opponent = opp;
	}

	
	/**
	 * Constructor for the local controller that receives data from 
	 * the remote player, gets inputs from the remote player via the
	 * Socket, sends move to the PlayGameModel which is on this local 
	 * machine.  Acts as host of the server.
	 * 
	 * @param opp the Username of your opponent
	 * @param port the port to host the Server on
	 */
	public PlayGameController(String opp, int port){
		System.out.println("Making Local Player 2(Server Host)");
		opponent = opp;
		pieceColor = 1;
		connected = false;
		
		//establish ServerSocket
		try{
			servSock = new ServerSocket(SSPORT);
			System.out.println("Made server on "+SSPORT);
		}catch(IOException e){
			System.out.println((e.getMessage())); 
			System.exit(1);
		}
		
		//start Thread to listen for connections and then start listening for messages
		startListening();
		
	}
	
	/**
	 * The Constructor for the remote player, gets input from the 
	 * remote player's PlayGameView, sends move to the socket, which 
	 * is read on the by the Host and send to the board.  Acts as the 
	 * client of the server.
	 * @param opp the Username of your opponent
	 * @param ip a String containing the IP Address of the game's host
	 * @param pt the port of the host Server
	 */
	public PlayGameController(String opp, String ip, int port){
		System.out.println("Making Remote Player 2 (Client Player)");
		
		opponent = opp;
		pieceColor = 1;
		connected = false;
		
		Socket cliSock = null;
		try{
			System.out.println("Connecting to "+ ip + " " + port);
			cliSock = new Socket( ip, port ) ;
			System.out.println("Connected, sending ..." ) ;
		}catch(IOException ex){
			System.out.println("Error: unable to connect to server." );
			System.out.println((ex.getMessage()));
		}
		gameView = new PlayGameView( this );
		
		// Start Thread to listen for connections and then start listening for messages
		// Set connected as true to skip attempting to listen for a client's connection
		makeConnection(cliSock);
		startListening();
		
	}
	
	public void setMaster(MasterClientController m) {
		masterCon = m;
	}

	/**
	 * The method to start the Host's thread to listen for connections 
	 * to the ServerSocket
	 */
	public void startListening(){

		worker = new Thread(this);
		worker.start();
	}
	
	/**
	 * Thread that waits for a connection from the opponent, called by 
	 * listenForOpponent, ends Thread after a connection has been made 
	 * and starts listening to that socket for messages.
	 */
	public void run(){
		Socket cliSock;
		while(!connected) {
			try {
				cliSock = servSock.accept();
				System.out.println(cliSock);
				//String info = "Client at " + sock.getInetAddress().getHostAddress() + " on port " +
					//	sock.getPort();
				//System.out.println(info);
				makeConnection(cliSock);
			} catch (Exception e) {
				System.out.println("Error accepting connection");
			}
		}
		
		startCommunication();
	}
	
	
	/**
	 * Creates Streams to communicate through the Socket with the client player
	 * @param cliSock the Socket of the client
	 */
	private void makeConnection(Socket cliSock) {
		System.out.println("Making Connection..." ) ;
		connected = true;
		sock = cliSock;
		
		try{
			in = sock.getInputStream();
			out = sock.getOutputStream();
			connected = true;
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}


	/**
	 * Starts listening for messages from the socket
	 */
	public void startCommunication() {	
		System.out.println("Connected, begining communication..." ) ;
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
						processMessage(msg);
					}
				}

			} catch (Exception e) {
				if(e.toString().equals("java.net.SocketException: Socket closed") )
					break;
				System.out.println( "Error receiving messages: " + e.toString());
			}
		}
	}


	/**
	 * Processes messages read in from the Socket and sends the 
	 * data to the correct functions.
	 * @param msg the message read from the Socket
	 */
	private void processMessage(String msg) {
		
		System.out.println("Color:"+pieceColor+" processing: "+msg);
		
		if(msg.contains(Tags.START_TURN)) {
			startTurn();
		}else if( msg.contains(Tags.MOVE)) {
			processMove( msg.substring(Tags.GAME_FEEDBACK.length(), msg.length()) );
		}else if( msg.contains(Tags.UPDATE_BOARD) ) {
			processUpdate( msg.substring(Tags.GAME_FEEDBACK.length(), msg.length()) );
		}else if(msg.contains(Tags.GAME_OVER)) {
			if(msg.contains(Tags.SUCCESS)) {endGame(true);}
			else {endGame(false);}
		}
	}

	/**
	 * Extracts the data from a move messages sent to this controller
	 * @param msg the message containing the move information
	 */
	private void processMove(String msg) {
		System.out.println("Color:"+pieceColor+" processing Move: "+msg);
		
		msg = msg.substring( Tags.MOVE.length(), msg.length() );
		String[] ar = msg.split(",");
		
		int r = Integer.parseInt(ar[0]);
		int c = Integer.parseInt(ar[1]);
		
		makeMove(r,c);
	}
	
	/**
	 * Extracts the data from an update board messages sent to this controller
	 * @param msg the message containing the update information
	 */
	private void processUpdate(String msg) {
		System.out.println("Color:"+pieceColor+" processing Update: "+msg);
	
		msg = msg.substring( Tags.UPDATE_BOARD.length(), msg.length() );
		String[] ar = msg.split(",");
		
		int b = Integer.parseInt(ar[0]);
		int r = Integer.parseInt(ar[1]);
		int c = Integer.parseInt(ar[2]);
		
		updateBoardView(b,r,c);
		
	}

	/**
	 * Sends a message to the Socket to get read from the other PlayerContoller
	 * @param msg the Message to send to the Socket
	 * @return a boolean descibing the success of the send
	 */
	private boolean sendMessage(String msg) {
		System.out.println("Color:"+pieceColor+" sending Message: "+msg);
		
		String message = msg+"\n";

		byte[] buff;
		buff = message.getBytes();

		try {
			out.write(buff, 0, message.length() );
			out.flush();
			return true;
		} catch ( Exception e ) {
			return false;
		}
	}

	/**
	 * Starts this player's turn. Has different functionality 
	 * based on what type of playerController this is.  
	 */
	public void startTurn() {
		System.out.println("Color:"+pieceColor+" starting turn");
				
		
		if(this.gameView != null && this.bot == null) {
			// If a view exists for this controller, then the player is local.
			gameView.startTurn();
		}else if(this.gameView == null && this.bot != null) {
			// If view doesn't exist but Gomobot does, ask it to take the turn
			bot.startTurn();
		}else {
			// If remote, send signal to socket to start remote turn
			startRemoteTurn();
		}		
	}
	
	/**
	 * Helper method to tell the remote player that their turn has started
	 */
	private void startRemoteTurn() {
		System.out.println("Color:"+pieceColor+" starting turn remotely.");
		sendMessage(Tags.GAME_FEEDBACK + Tags.START_TURN);
	}


	/**
	 * updates the visable board that the player sees in PlayGameController
	 * 
	 * @param b indicates whether to place a black or white game piece. 
	 * 			0=white, 1=black
	 * @param r the row where the user wants to place a piece
	 * @param c the column where the user places a piece
	 */
	public void updateBoardView(int b, int r, int c) {
		System.out.println("in GameCon.updatedateBoardView (Color: "+pieceColor+") with:"+b+","+r+","+c);
		
		
		if(this.gameView != null && bot == null) {
			// If a view exists and there is not bot for this controller, then the player is local.
			gameView.updateBoard(b,r,c);
		}else if(this.gameView == null && this.bot != null) {
			// If there is not gameView and a bot, update the bot's board"view"
			bot.updateBoard(b,r,c);
		}else {
			// If remote, send signal to socket to update their view
			updateRemoteView(b, r, c);
		}
		
	}
	
	/**
	 * Helper method to tell the remote player how to update their board
	 * @param b indicates whether to place a black or white game piece. 
	 * 			1=white, 2=black
	 * @param r the row where the user wants to place a piece
	 * @param c the column where the user places a piece
	 */
	private void updateRemoteView(int b, int r, int c) {
		System.out.println("Color:"+pieceColor+" updating view remotely with:"+b+","+r+","+c);
		sendMessage(Tags.GAME_FEEDBACK+Tags.UPDATE_BOARD+b+","+r+","+c);
	}
	
	/**
	 * Sends a move to the Game Model
	 * @param r the row where the user wants to place a piece
	 * @param c the column where the user places a piece
	 */
	public void makeMove(int r, int c) {
		System.out.println("in GameCon.makeMove (Color: "+pieceColor+") with:"+r+","+c);
		
		// If a model exists for this controller, it is the local player
		if(this.gameModel != null) {
			gameModel.updateBoard(pieceColor,r,c);
		}else {
			// It's the remote controller, send the move over the socket
			makeRemoteMove(r,c);
		}
	}

	/**
	 * Helper method to tell the remote playerController how this player made their move
	 * @param r the row where the user wants to place a piece
	 * @param c the column where the user places a piece
	 */
	private void makeRemoteMove(int r, int c) {
		System.out.println("Color:"+pieceColor+" makingMove remotely with:"+r+","+c);
		sendMessage(Tags.GAME_FEEDBACK+Tags.MOVE+r+","+c);		
	}


	
	/**
	 * getter and Setters for pieceColor
	 */
	public int getPieceColor() {
		return pieceColor;
	}
	public void setPieceColor(int pieceColor) {
		this.pieceColor = pieceColor;
	}


	/**
	 * Setter for GameModel
	 * @param gameModel
	 */
	public void setGameModel(GameModel gameModel) {
		this.gameModel = gameModel;
	}
	
	/**
	 * A method to determine if this PlayGameController has 
	 * connected to a game.  If so, the game is ready to 
	 * start.
	 * @return a boolean describing this Controller's conection status
	 */
	public boolean isConnected() {
		return this.connected;
	}
	
	/**
	 * Called when the GameModel determines that the game is over.
	 * @param vict a boolean referring to whether or not the player won the game
	 */
	public void endGame(boolean vict) {
		System.out.println("ending game..."+vict);
		
		this.victory = vict;
		//only do this if its the local player, not the remote
		System.out.println("gameV: "+gameView);
		
		
		// If a view exists for this controller, then the player is local.
		if(this.gameView != null) {
			disconnect();
			gameView.endGame(this.victory);
		}else {
			// If remote, send signal to socket to update their view
			endRemoteGame(this.victory);
		}
	}


	/**
	 * Helper method to tell the remote player to end the Game.
	 * @param vict a boolean referring to whether or not the player won the game
	 */
	private void endRemoteGame(boolean vict) {
		System.out.println("ending remote game win? "+vict);
		
		if(vict) {
			sendMessage(Tags.GAME_FEEDBACK+Tags.GAME_OVER+Tags.SUCCESS);
		}else {
			sendMessage(Tags.GAME_FEEDBACK+Tags.GAME_OVER+Tags.FAIL);
		}
			
	}


	/**
	 * Ends the program and notifies the Server who was the winner.
	 * If a player disconnected before the game was over, it 
	 * counts as a loss.
	 */
	public void disconnect() {

		if(victory) { // You won!
			masterCon.sendMessage(Tags.GAME_FEEDBACK+masterCon.getUsername()+","+opponent);
		}else {		  // You lost, or you quit
			masterCon.sendMessage(Tags.GAME_FEEDBACK+opponent+","+masterCon.getUsername());
		}
		masterCon.disconnect(2);
		
		try {
			if(this.servSock != null) {
				servSock.close();
			}else if(this.sock != null){
				sock.close();
			}
		}catch (Exception e) {
			System.out.println("error closing socket");
		}
	}

}