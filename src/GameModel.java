/**
 * GameModel manages the Gomoku's rules of gameplay and manages the
 * the two players in going between who's turn it is, updating the
 * board and checking to see if the most recent move won.
 * @author Emma Poffenberger
 * 

 */


public class GameModel {

	private PlayGameController player1;
	private PlayGameController player2;
	private int[][] board;
	private final int SIZE = 30;
	private int turn;
	private int moves;
	private int pieces;	
	private boolean gameover = false;
	private boolean tie = false;
	
	/*
	 * @param: plr1		playGameController object
	 * @paramL plr2 	playGameController object
	 * board[][] key:
	 * 		0 = empty space
	 * 		1 = player 1 in space
	 * 		2 = player 2 in space
	 */
	public GameModel( PlayGameController plr1, PlayGameController plr2) {
		player1 = plr1;
		player2 = plr2;
		
		player1.setGameModel(this);
		player2.setGameModel(this);
		
		board = new int[SIZE][SIZE];
	}
	
	/**
	 * Starts the game by initializing turns and calling startTurn on player1
	 */
	public void startGame() {
		
		System.out.println("GameModel.startGame");
		
		turn = 1;
		moves = 0;
		player1.startTurn();
		
	}

	/*
	 * @param: x	the row of the move
	 * @param: y	the column of the move
	 * 
	 * 
	 */
	public void updateBoard(int c, int x, int y ) {
		
		System.out.println("in GameMod.updateBoard with:"+c+","+x+","+y);
		// update board to include the tile 
		if(x >= 0 && y >= 0) {
			// This means a move was made
			board[x][y] = turn;
			if( checkGameOver(x, y) ) {
				gameover = true;
				if( turn == 1 )
					boadcastEndGame( player2 );
				if( turn == 2 )
					boadcastEndGame( player1 );
			}else {
				// Game isn't over, broadcast the move
				broadcastMove(c,x,y);
			}
		}
		
		// Otherwise time ran out, don't change the board, just update turn
		if(!gameover)
			updateTurn();
	}
	
	private void boadcastEndGame(PlayGameController player12) {
		if( !tie ) {
			player1.endGame(false);
			player2.endGame(false);
		}
		System.out.println("THE GAME HAS BEEN WON");
		if( player12 == player1 ) {
			player1.endGame(false);
			player2.endGame(true);
		}
		else if( player12 == player2 ) {
			player2.endGame(false);
			player1.endGame(true);
		}
	}

	/**
	 * Sends the newly made move to the other player
	 */
	private void broadcastMove(int c, int x, int y) {
	
		if(turn==1) {
			System.out.println("Broadcasting move to Player2");
			player2.updateBoardView(c, x, y);
		}else if(turn==2) {
			System.out.println("Broadcasting move to Player1");
			player1.updateBoardView(c, x, y);
		}
			
			
				
	}

	/*
	 * Switches turn from 1 to 2
	 * 1 = white/player1 & 2 = black/player2
	 * updates move to add one for a successful move;
	 */
	private void updateTurn() {
		if( turn == 1 ) {
			turn = 2;
			System.out.println("telling player2 to start turn");
			player2.startTurn();
		} else if( turn == 2 ) {
			turn = 1;
			System.out.println("telling player1 to start turn");
			player1.startTurn();
			++moves;
		}
	}
	
	/*
	 * @param: x
	 * @param: y
	 * @param: win
	 */
	private boolean checkGameOver( int x, int y) {	
		int spaces = 0;
		for( int i = 0; i < board.length; i++ ) {
			for( int k = 0; k < board.length; k++ ) {
				if( board[i][k] == 0 ) {
					spaces += 1;
				}
			}
		}
		if( spaces > 0 ) {
			System.out.println("Checking Gameover..."); 
			pieces = 0;
			if (checkHorizontal( x, y) )
				return true;
			else if( checkVertical( x, y) )
				return true;
			else if( checkDiagonalLeftRight( x, y) )
				return true;
			else if( checkDiagonalRightLeft( x, y) )
				return true;
			else 
				return false;
		}
		else {
			tie = true;
			return false;
		}
		
		
		
	}
	
	//Check Rows
	private boolean checkHorizontal( int x, int y) {
		boolean r = false;
		if( board[x][y] == turn ) {
			pieces++;
			// Search the board horizontally right:
			if( y+1 < SIZE && board[x][y+1] == turn) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( y+2 < SIZE && board[x][y+2] == turn ) {
					pieces++;
					if( checkPieces() )
						r = true;
					if( y+3 < SIZE && board[x][y+3] == turn) {
						pieces++;
						if( checkPieces() )
							r = true;
						if(y+4 < SIZE && board[x][y+4] == turn) {
							pieces++;
							if( checkPieces() )
								r = true;
			} } } }
			// Search the board horizontally left:
			if( y-1 >= 0 && board[x][y-1] == turn ) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( y-2 >= 0 && board[x][y-2] == turn) {
					pieces++;
					if( checkPieces() )
						r = true;
					if( y-3 >= 0 && board[x][y-3] == turn ) {
						pieces++;
						if( checkPieces() )
							r = true;
						if( y-4 >= 0 && board[x][y-4] == turn ) {
							pieces++;
							if( checkPieces() ) 
								r = true;
			} } } }	System.out.println("Checking horz, p="+pieces); 
		}else {
			pieces = 0;
			r = false;
		}
		pieces = 0;
		return r;
	}
	
	//Check Columns 
	private boolean checkVertical( int x, int y) {
		boolean r = false;
		
		if( board[x][y] == turn ) {
			pieces++;
			// Search the board vertically up:
			if( x-1 >= 0 && board[x-1][y] == turn ) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( x-2 >= 0 && board[x-2][y] == turn ) {
					pieces++;
					if( checkPieces() )
						r = true;
					if( x-3 >= 0 && board[x-3][y] == turn ) {
						pieces++;
						if( checkPieces() )
							r = true;
						if( x-4 >= 0 && board[x-4][y] == turn ) {
							pieces++;
							if( checkPieces() )
								r = true;
			} } } }
			// Search the board vertically down:
			if( x+1 < SIZE && board[x+1][y] == turn ) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( x+2 < SIZE && board[x+2][y] == turn ) {
					pieces++;
					if( checkPieces() )
						r = true;
					if( x+3 < SIZE && board[x+3][y] == turn ) {
						pieces++;
						if( checkPieces() )
							r = true;
						if( x+4 < SIZE && board[x+4][y] == turn ) {
							pieces++;
							if( checkPieces() ) 
								r = true;
			} } } } System.out.println("Checking Vert, p="+pieces); 
		}else {
			pieces = 0;
			r = false;
		}
		
		pieces = 0;
		return r;
	}
	
	//Check Diagonal left to right
	private boolean checkDiagonalLeftRight( int x, int y) {
		boolean r = false;
		if( board[x][y] == turn ) {
			//increment number of pieces in a row 
			pieces++;
			// searches the board up to the left == check for 5 pieces in a row after each new piece found
			 if( x-1 >= 0 && y-1 >= 0 && board[x-1][y-1] == turn   ) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( x-2 >= 0 && y-2 >= 0 && board[x-2][y-2] == turn ) {
					pieces++;
					if( checkPieces() )
						r = true;
					if( x-3 >= 0 && y-3 >= 0 && board[x-3][y-3] == turn ) {
						pieces++;
						if( checkPieces() )
							r = true;
						if( x-4 >= 0 && y-4 >= 0 && board[x-4][y-4] == turn ) {
							pieces++;
							if( checkPieces() )
								r = true;
			} } } } // searches the board down to the right
			if( x+1 < SIZE && y+1 < SIZE  && board[x+1][y+1] == turn ) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( x+2 < SIZE && y+2 < SIZE && board[x+2][y+2] == turn ) {
					pieces++;
					if( checkPieces() )
						r = true;
					if( x+3 < SIZE && y+3 < SIZE && board[x+3][y+3] == turn ) {
						pieces++;
						if( checkPieces() )
							r = true;
						if( x+4 < SIZE && y+4 < SIZE && board[x+4][y+4] == turn ) {
							pieces++;
							if( checkPieces() )
								r = true;
			} } } } System.out.println("Checking Diag LR, p="+pieces); 
		} else {
			pieces = 0;
			r = false;
		}
		pieces = 0;
		return r;
	}
	
	//Check Diagonal right to left
	private boolean checkDiagonalRightLeft( int x, int y) {
		boolean r = false;
		if( board[x][y] == turn ) {
			//increment number of pieces in a row 
			pieces++;
			// searches the board up to the right == check for 5 pieces in a row after each new piece found
			if( x-1 >= 0 && y+1 < SIZE && board[x-1][y+1] == turn ) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( x-2 >= 0 && y+2 < SIZE && board[x-2][y+2] == turn ) {
					pieces++;
					if( checkPieces() )
						r = true;
					if(x-3 >= 0 && y+3 < SIZE && board[x-3][y+3] == turn ) {
						pieces++;
						if( checkPieces() )
							r = true;
						if(x-4 >= 0 && y+4 < SIZE && board[x-4][y+4] == turn ) {
							pieces++;
							if( checkPieces() )
								r = true;
			} } } } // searches the board down to the left
			if( x+1 < SIZE && y-1 >= 0 && board[x+1][y-1] == turn ) {
				pieces++;
				if( checkPieces() )
					r = true;
				if( x+2 < SIZE && y-2 >= 0 && board[x+2][y-2] == turn ) {
					pieces++;
					if( checkPieces() )
						r = true;
					if( x+3 < SIZE && y-3 >= 0 && board[x+3][y-3] == turn ) {
						pieces++;
						if( checkPieces() )
							r = true;
						if( x+4 < SIZE && y-4 >= 0 && board[x+4][y-4] == turn ) {
							pieces++;
							if( checkPieces() )
								r = true;
			} } } } System.out.println("Checking Diag RL, p="+pieces); 
		} else{
			pieces = 0;
			r = false;
		}
		pieces = 0;
		return r;
	}
	
	
	private boolean checkPieces() {
		System.out.print(" "+pieces);
		if( pieces == 5 ) {
			System.out.println("GameModel.checkPieces = TRUE---");
			return true;
		}else {
			return false;
		}
			
		
			
	}
}
