/**
 *@author Natalie Stephenson
 *"AI" that is not really an AI, but a program that chooses moves as an 
 * opponent in Gomoku. Gomobot!
 *
 */

import java.util.Random; 
public class Gomobot{

	final int BOARDSIZE = 30;
	final int SEARCH_RADIUS = 3;
	private int[][] board;
	private PlayGameController controller; 
	private String difficulty; 
	private int pieceColor;
	private int numMoves;

	//constructor for AI
	public Gomobot(PlayGameController p, String diff){
		difficulty = diff;
		controller = p; 
		board = new int[BOARDSIZE][BOARDSIZE];
		pieceColor = controller.getPieceColor();
	}
	
	//called by the controller, telling it to make a move
	public void startTurn(){
		if(this.difficulty.equals("EASY")){
			makeMoveEasy(); 
		}
		else if(this.difficulty.equals("MEDIUM")){
			makeMoveMedium(); 
		}
		else if(this.difficulty.equals("HARD")){
			makeMoveHard(); 
		}

	}

	
	/**
	 * The move generator for the EASY Gomobot. Randomly selects 
	 * a place on the board to move and checks if it is clear. 
	 * If so, it send the move to the controller.
	 */
	private void makeMoveEasy(){
		
		//something like this
		boolean done = false; 
		int r = -2,c = -2;
		while(!done){
			r = (int) Math.floor(Math.random()*BOARDSIZE);
			c = (int) Math.floor(Math.random()*BOARDSIZE);
			done = isOpen(r, c);
		}
		System.out.println("Gomobot Making Easy move: "+r+","+c);
		numMoves++;
		controller.makeMove(r,c); 
		board[r][c] = pieceColor;
	}

	private boolean isOpen(int r, int c){
		boolean ret = false;
		if(board[r][c] == 0)
			ret = true;
		return ret;
	}

	private void makeMoveMedium(){

		// choose a random location
		int startC = (int) Math.floor(Math.random()*BOARDSIZE); 
		int startR = (int) Math.floor(Math.random()*BOARDSIZE); 
		
		if(numMoves == 0) {
			numMoves++;
			controller.makeMove(3, 3);
			board[startR][startC] = pieceColor;
		}else {
			int[] move = findPiece(startR, startC);
			
			if(move != null) {
				System.out.println("Gomobot Making Medium move: "+move[0]+","+move[1]);
				numMoves++;
				controller.makeMove(move[0], move[1]);
				board[move[0]][move[1]] = pieceColor;
			}else {
				makeMoveEasy();
			}
			
		}	
	}
	
	
	private int[] findPiece(int startR, int startC) {
		
		int[] arr = new int[]{0,0};
		int r = (startR+1)%BOARDSIZE, c = 0;
		boolean doneSearching = false, done = false;
		int row,col;
		while(!doneSearching) {
			
			// Find a piece of my color
			while(!done) {
				while(!done && c < BOARDSIZE && r < BOARDSIZE) {
					System.out.println("GomoBot: "+r+","+c+": "+board[r][c]);
					if(board[r][c] == pieceColor){
						arr[0] = r;
						arr[1] = c;
						done = true;
						System.out.println("GomoBot found a piece");
					}else {
						c++;
					}
				}
				c = 0;
				r = (r+1)%BOARDSIZE;
			}
			
			System.out.println("Gomobot seaching for empty spot");
			// Find an empty spot around it
			row = arr[0];
			col = arr[1];
			if(row>0 && col>0 && board[row-1][col-1]==0) {
				arr[0] = row-1;
				arr[1] = col-1;
				doneSearching = true;
			}else if(col>0 && board[row][col-1]==0)  {
				arr[0] = row;
				arr[1] = col-1;
				doneSearching = true;
			}else if(row<BOARDSIZE && col>0 && board[row+1][col-1]==0) {
				arr[0] = row+1;
				arr[1] = col-1;
				doneSearching = true;
			}else if(row>0 && board[row-1][col]==0) {
				arr[0] = row-1;
				arr[1] = col;
				doneSearching = true;
			}else if(row<BOARDSIZE && board[row+1][col]==0) {
				arr[0] = row+1;
				arr[1] = col;
				doneSearching = true;
			}else if(row>0 && col<BOARDSIZE && board[row-1][col+1]==0) {
				arr[0] = row-1;
				arr[1] = col+1;
				doneSearching = true;
			}else if(col<BOARDSIZE && board[row][col+1]==0) {
				arr[0] = row;
				arr[1] = col+1;
				doneSearching = true;
			}else if(col<BOARDSIZE && row<BOARDSIZE && board[row+1][col+1]==0) {
				arr[0] = row+1;
				arr[1] = col+1;
				doneSearching = true;
			}
			System.out.println("didnt find one this time");
			if(!doneSearching) {
				doneSearching = true;
				done = false;
			}
		}
		if(!done)
			return null;
		else
			return arr;
	}

	private void makeMoveHard(){
		//blahblahblah
	}

	public void updateBoard(int b, int r, int c) {
		board[r][c] = b;
	}



} 