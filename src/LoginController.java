import java.io.IOException;

/**
 * @author Natalie Stephenson
 * LoginController - Controller for the LoginView GUI.
 * This reads user input from the LoginView and relays
 * the data to the MasterController.  
 * 
 * @author Emily Shane
 * Updated to implement use of Tags class to eliminate 
 * String literals
 * 
 * UPDATE 4/23/18 (EMW): Updated to recognize when a player 
 *       is already online and added functionality 
 *       to store an anonymous username locally.
 *
 */
public class LoginController {

	//declare fields
	private MasterClientController masterCon; 
	private LoginView loginView; 
	
	/**
	 * constructor for LoginController. initializes fields
	 * @param m the MasterController to which this LoginController
	 * must relay information
	 *  */
	public LoginController( MasterClientController m){
		this.masterCon = m;
		
		//create a new LoginView
		//pass this LoginController to the LoginView
		loginView = new LoginView(this);
		
		
	}
	
	/**
	 * authenticate method. initiates authentication procedures.
	 * reads incoming data from LoginView, and 
	 * calls appropriate methods to relay data back to MasterController.
	 * @param info, a String[] array of data sent directly from the loginView's listeners
	 * @return a boolean - true if successful, false if not
	*/
	public boolean authenticate(String[] info){ 
		
		//first check to see if we're connected to the server
		if(!this.masterCon.isConnected() ) {
			this.masterCon.connectToServer(info[5], Integer.parseInt(info[4]));
			//this.masterCon.startListening();
		}
		
		//info[0] = action tag (REG for register, LOGIN for login, ANON for anonymous)
		//info[1] = String options for gametype: AI1, AI2, AI3, Online 
		//info[2] = message username
		//into[3] = message password
		
		// set tags
		String tag = "";
		String message = "";
		//tag, then gametype, username, password
		
		//check accuracy/legitimacy of tag (can be removed later)
		if(info[0].equals(Tags.REGISTER)){ tag = Tags.REGISTER;}
		else if(info[0].equals(Tags.LOGIN)){ tag = Tags.LOGIN;}
		else if(info[0].equals(Tags.ANON)){tag = Tags.ANON; }
		else{ 
		    
		}
		
		//set Game Type by sending it to MasterController
		masterCon.setGameType(info[1]);
		//set the name of this user in MasterController
		masterCon.setUsername(info[2]);
		
		//if not playing anonymously (login/register), gather username and password;
		if(tag.equals(Tags.REGISTER) || tag.equals(Tags.LOGIN) ){
			//append tag, username/password of user: "[TYPE]username password"
			message = tag + info[2] + " " + info[3]; 
			//relay to MasterController
			masterCon.sendMessage(message);
		}   //otherwise, just send tag
		else{ masterCon.sendMessage(tag);}
		
		return true;
	}
	
	/**
	 * updateMessage displays messages to the LoginView. 
	 * @param s the string to interpret
	 * */
	public void updateMessage(String s){
		
		if(s.startsWith(Tags.SUCCESS)){ 
			loginView.dispose();
			masterCon.toMatchMaking();
		}else if(s.startsWith(Tags.UN_NOT_FOUND)){ 
			loginView.updateMessage("Error: that username doesn't exist!");
		}else if(s.startsWith(Tags.WRONG_PASS)){
			loginView.updateMessage("Error: incorrect password");
		}else if(s.startsWith(Tags.UN_TAKEN)){
			loginView.updateMessage("Error: Someone has already taken that username.");
		}else if(s.startsWith(Tags.UPDATEONLINE)) {
			loginView.updateMessage("Error: That user is already logged in.");
		}else {
			loginView.updateMessage(s);
		}
	}
	
	/**
	 * handle info from the server. In this controller, the
	 * the message contains this players username, which is set in this method.
	 * @param s the string to interpret
	 */
	public void processMessage(String s){
		
		System.out.println("do i get here?");
		if(s.startsWith(Tags.SUCCESS)) {
			String un = s.substring(Tags.SUCCESS.length(), s.length());
			masterCon.setUsername(un);
		}
		

		updateMessage(s);
	}

	public void playLocal(String gt) {
		String difficulty = "";
		if(gt.contains("Easy"))
			difficulty = "EASY";
		if(gt.contains("Medium"))
			difficulty = "MEDIUM";
		if(gt.contains("Hard"))
			difficulty = "HARD";
		
		masterCon.offlineGame(difficulty);
	}

	/**
	 * tell the server that this client is disconnecting
	 */
	public void disconnect(){
		masterCon.disconnect(0);
	}

}
