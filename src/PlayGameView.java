import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.File;
import java.awt.Color;
import java.util.ArrayList;

/**
 * @author NatalieStephenson
 * @author EmilyShane
 * The class to create the GUI for the GamePlay...
 * TODO clean up the import statements lol
 *
 */
public class PlayGameView extends JFrame {
	private PlayGameController gameCon;
	int[][] boardImages;
	JLabel[][] boardTiles;
	JPanel gamePanel;
	JPanel panelMain;
	JButton moveButton;
	JButton forfeitButton;
	BufferedImage emptyTile;
	BufferedImage whiteTile;
	BufferedImage blackTile;
	final int BOARDSIZE = 30;
	final int CROSSX = 26;
	final int CROSSY = 20;
	MyMouseListener mouse;
	final String WHITETILE = "small_whitepiecetile.jpg";
	final String BLACKTILE = "small_blackpiecetile.jpg";
	final String EMPTYTILE = "small_emptytile.jpg";
	int numMoves = 0 ;//number of moves for this player in this game
	JTextArea movesArea;
	JTextArea turnArea;
	JTextArea clockArea;
	boolean myTurn ;
	int myColor;
	private int currentRow = -2, currentCol = -2;

	public PlayGameView (int a){

		buildView();
		initEventHandlers();

		setTitle("Gomoku");
		setSize(1000,700);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.setVisible(true);

	}

	public PlayGameView ( PlayGameController gamecon ) {
		this.gameCon = gamecon;
		myColor = gameCon.getPieceColor();
		buildView();
		initEventHandlers();

		setTitle("Gomoku");
		setSize(1000,700);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.setVisible(true);
	}

	//builds the initial GUI
	public void buildView() {

		panelMain = new JPanel();
		panelMain.setLayout(new BorderLayout(0, 0));

		//Top banner
		JTextField topBanner = new JTextField();
		topBanner.setEditable(false);
		topBanner.setBackground(SystemColor.control);
		topBanner.setHorizontalAlignment(SwingConstants.CENTER);
		topBanner.setText("GAME!!");
		topBanner.setPreferredSize( new Dimension(500, 25));
		topBanner.setBorder( new EmptyBorder(5, 5, 5, 5));

		//game board
		gamePanel = new JPanel(new GridLayout(0, 30, 1,1));
		gamePanel.setBackground(new Color(230, 212, 170));
		gamePanel.setBorder( new EmptyBorder(5, 5, 5, 5));
		gamePanel.setPreferredSize( new Dimension(510, 510));

		//load texture backgrounds to gamePanel! (start with the empty tiles)
		boardImages = new int[BOARDSIZE][BOARDSIZE];
		boardTiles = new JLabel[BOARDSIZE][BOARDSIZE];

		try{
			emptyTile = ImageIO.read(new File(EMPTYTILE));
			whiteTile = ImageIO.read(new File(WHITETILE));
			blackTile = ImageIO.read(new File(BLACKTILE));
		}catch(IOException ex){
			System.out.println("Error: cannot load texture image.\n" +
					"PlayGameView buildview");
		}
		for(int i = 0; i<30; i++){
			for(int j=0; j<30; j++){
				//set all values of the boardImages to 0 = emptyTile
				boardImages[i][j] = 0;
			}
		}

		drawBoard();

		panelMain.add(gamePanel);

		//sidebar for other game information (aligned right)
		JPanel sideBoard = new JPanel();
		sideBoard.setBackground(SystemColor.control);
		sideBoard.setLayout(new BorderLayout());
		sideBoard.setBorder( new EmptyBorder(5, 5, 5, 5));
		sideBoard.setPreferredSize( new Dimension(175, 300));

		panelMain.add(sideBoard, BorderLayout.EAST);

		//area indicating whose turn it is
		turnArea = new JTextArea();
		turnArea.setLayout(new BorderLayout());
		turnArea.setBorder( new LineBorder(new Color(0, 0, 0)));
		turnArea.setText(" Whose turn is it?\n OPPONENT'S TURN");
		turnArea.setPreferredSize(new Dimension( 100, 70));
		turnArea.setEditable(false);

		sideBoard.add(turnArea, BorderLayout.NORTH);

		//panel for moves & clock
		JPanel movesPanel = new JPanel();
		movesPanel.setLayout(new BorderLayout());

		//area for user's number of moves
		movesArea = new JTextArea();
		movesArea.setBorder( new LineBorder(new Color(0,0,0)));
		movesArea.setText("Moves You've Made: ");
		movesArea.setEditable(false);
		movesArea.setPreferredSize(new Dimension(100, 70));
		movesPanel.add(movesArea, BorderLayout.NORTH);

		//timer area
		clockArea = new JTextArea();
		clockArea.setBorder( new LineBorder(new Color(0,0,0)));
		clockArea.setText("TIMER\n");
		clockArea.setEditable(false);
		clockArea.setPreferredSize(new Dimension(100, 90));
		movesPanel.add(clockArea, BorderLayout.CENTER);


		sideBoard.add(movesPanel, BorderLayout.CENTER);

		//forfeit button -- add this later
		forfeitButton = new JButton();
		forfeitButton.setText("FORFEIT");
		forfeitButton.setPreferredSize(new Dimension( 100, 30));

		//move button
		moveButton = new JButton();
		moveButton.setText("MOVE");
		moveButton.setPreferredSize(new Dimension( 100, 30));

		sideBoard.add(moveButton, BorderLayout.SOUTH);
		this.getContentPane().add(panelMain);

		
		System.out.println("done building view");

	}
	/**
	 *updateBoard updates the game board to display the appropriate
	 * icons given the desired icon and position of a player's move
	 * @param blackOrWhite indicates whether to place a black or white
	 * game piece. 0=blank, 1=white, 2=black
	 * @param row the row where the user wants to place a piece
	 * @param col the column where the user places a piece
	 */
	public void updateBoard(int blackOrWhite, int row, int col){
		
		boardImages[row][col] = blackOrWhite;
		//redraw the board
		//drawBoard();
		JLabel temp = boardTiles[row][col];
		switch (blackOrWhite){
			case 0:
				temp.setIcon(new ImageIcon(emptyTile));
				break;
			case 1:
				temp.setIcon(new ImageIcon(whiteTile));
				break;
			case 2:
				temp.setIcon(new ImageIcon(blackTile));
				break;
			default:
				break;
		}
	}


	/**
	 * drawBoard draws the board according to the current state.
	 * It reselects and draws each image on the board to reflect the
	 * given state.
	 */
	public void drawBoard(){
		JPanel newGamePanel = new JPanel(new GridLayout(0, 30, 1,1));
		newGamePanel.setBackground(new Color(230, 212, 170));
		newGamePanel.setBorder( new EmptyBorder(5, 5, 5, 5));
		newGamePanel.setPreferredSize( new Dimension(510, 510));

		for(int i = 0; i<30; i++){
			for(int j=0; j<30; j++){
				JLabel textureLabel = new JLabel();

				if(boardImages[i][j] == 0){ //empty tile
					textureLabel.setIcon(new ImageIcon(emptyTile)) ;

					newGamePanel.add(textureLabel);
				}
				else if(boardImages[i][j] == 1){ // white tile
					textureLabel.setIcon(new ImageIcon(whiteTile));
					newGamePanel.add(textureLabel);

				}
				else if(boardImages[i][j] == 2) { //black tile
					textureLabel.setIcon(new ImageIcon(blackTile));
					newGamePanel.add(textureLabel);
				}

				boardTiles[i][j] = textureLabel;
			}
		}
		panelMain.remove(gamePanel);
		gamePanel = newGamePanel;
		initMouseHandlers();
		panelMain.add(gamePanel);
		this.getContentPane().add(panelMain);
	}

	protected void startTurn(){
		System.out.println("in GameView.startTurn, r: "+currentRow+","+currentCol);
		turnArea.setText(" Whose turn is it?\n MY TURN");
		if(currentRow != -2 && currentCol != -2 && boardImages[currentRow][currentCol] == myColor ){
			updateBoard(0, currentRow, currentCol);
		}
		
		currentCol = -2;
		currentRow = -2;
		myTurn = true; //at end of makeMove, set myTurn back to false (when move is sent to controller)
		startTimer();
	}

	Thread timerThread;
	private void startTimer() {
		
		timerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				int numSec = 120;
				while(myTurn && numSec > 0) {
					clockArea.setText(" TIMER:\n "+numSec+" seconds remaining");
					numSec--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Player made a move in time
					}
				}
				
				if(numSec == 0) {// ran out of time
					endTurn();
					makeMove(-1, -1);
				}
					
			}

		});
		
		timerThread.start();
	}

	

	protected void endTurn() {
		System.out.println("in GameView.endTurn");
		turnArea.setText(" Whose turn is it?\n OPPONENT'S TURN");
		timerThread.interrupt();
		myTurn = false;
				
	}

	//updateTurn: updates the turnArea on the GUI
	//@param: s, a string given by the Controller representing
	//what should be displayed about whose turn it is
	public void updateTurn(String s){
		System.out.println("in GameView.updateTurn");
		turnArea.setText(s);
	}

	//updateClock: updates the current clock/timer display
	public void updateClock(){

	}

	//updateNumMoves: called by the controller to update now many moves
	//this user has made in this game
	public void updateNumMoves(){
		numMoves++;
		movesArea.setText("Moves you've made:\n\t" + numMoves);
	}

	public boolean checkOpen(int row, int col){
		return boardImages[row][col]==0;
	}

	public void makeMove(int row, int col){
		
		if(row >= 0 && col >= 0) {
			// Turn was made normally
			boardImages[row][col] = myColor;
			currentCol = -2;
			currentRow = -2;
		}
		// Time ran out or some sort of error, pass on turn
		updateNumMoves();
		gameCon.makeMove(row, col);
	}

	private void initEventHandlers() {
		mouse = new MyMouseListener();
		initMouseHandlers();
		ActionListener moveListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e){
				if(e.getActionCommand().equals("MOVE")){
					if(myTurn) {
						endTurn();
						makeMove(currentRow, currentCol);
					}
				}
			}
		};
		moveButton.addActionListener(moveListener);

		//Exiting
		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				exitProcedure();
			}
		});

	}

	private void initMouseHandlers(){
		gamePanel.addMouseListener(mouse);
	}

	private class MyMouseListener extends MouseInputAdapter{

		@Override
		public void mouseClicked(MouseEvent e){
			int xClick = e.getX();
			int yClick = e.getY();


			int row = findMouseRow(yClick);
			int col = findMouseCol(xClick);

			if(row != -1 && col != -1){
				handleClick(row, col);
			}
		}


		public int findMouseCol(int x){
			int col = -1;
			if(x > 17 && x < 797) {
				int endX = 17;
				int colCount = 0;
				while (col == -1 && endX < 797) {
					endX += 26;
					if (x <= endX) {
						col = colCount;
					}
					colCount++;
				}
			}
			return col;
		}

		public int findMouseRow(int y){
			int row = -1;
			if(y > 17 && y < 648) {
				int endY = 17;
				int rowCount = 0;
				while (row == -1 && endY < 648) {
					endY += 21;
					if (y <= endY) {
						row = rowCount;
					}
					rowCount++;
				}
			}
			return row;
		}

		private void handleClick(int row, int col){
			//validate move
			if(checkOpen(row, col)){
				if(currentRow != -2 && currentCol != -2){
					updateBoard(0, currentRow, currentCol);
				}
				updateBoard(myColor, row, col);
				currentRow = row;
				currentCol = col;
			}

		}
	}
	
	public void endGame(boolean victory) {
		System.out.println("in gameView endgame");
		myTurn = false;
		if(victory) {
			turnArea.setText(" CONGRATULATIONS!!\n YOU WIN.\n Close window to\n return to lobby");
		}else {
			turnArea.setText(" TOO BAD!!\n YOU LOSE.\n Close window to\n return to lobby");
		}
		
	}

	protected void exitProcedure() {
	
		System.out.println("in playgame exit");
		// Disconnect the Controller
		gameCon.disconnect();
		
		// Close this window
		
	}

	
}
