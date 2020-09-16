/**
 * The Tags class is designed for an implementation of a virtual Gomoku game. 
 * These tags are used by both the client and server sides to allow each
 * class to interpret the messages that it receives from other actors and 
 * classes. Since most of the communication happens over Sockets, these tags
 * must be Strings that can be concatenated onto the front of other messages.
 * 
 * @author Emily Shane
 * @author Ethan Wiederspan
 */

public class Tags {
	//tags that show the server what the client needs
	public static final String REGISTER = "[REG]";
	public static final String LOGIN = "[LOGIN]";
	public static final String ANON = "[ANON]";
	public static final String DISCONNECT = "[DISCON]";
	public static final String INVITE = "[SENDINV]";
	public static final String RESPOND = "[RSPINV]";
	public static final String QUIT = "[QUIT]";
	public static final String UPDATEONLINE = "[UPDTUSERSONLINE]";
	public static final String ADDUSER = "[NEWUSER]";
	public static final String REMOVEUSER = "[RMVUSER]";
	public static final String INGAME = "[INGAME]";
	public static final String HOST = "[HOST]";
	
	
	//tags that show the user what the server is doing
	public static final String AUTH_FEEDBACK = "[AUTH]";
	public static final String MATCH_FEEDBACK = "[MATCH]";
	public static final String GAME_FEEDBACK = "[GAME]";
	
	//confirmation and error messages
	public static final String SUCCESS = "SUCCESS";
	public static final String FAIL = "FAILURE";
	public static final String UN_NOT_FOUND = "NOUSER";
	public static final String WRONG_PASS = "BADPASS";
	public static final String UN_TAKEN = "USERTAKEN";
	public static final String CONFIRM = "[CONF]";
	public static final String DENY = "[DENY]";
	public static final String CANCELINVITE = "[CNCLUSR]";
	
	//gameplay tags
	public static final String START_TURN = "[TURN]";
	public static final String MOVE = "[MOVE]";
	public static final String UPDATE_BOARD = "[BOARD]";
	public static final String GAME_OVER = "[GAMEOVER]";
	
}
