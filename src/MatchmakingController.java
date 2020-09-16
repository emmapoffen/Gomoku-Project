/**
 * @author NatalieStephenson
 * @author EthanWiederspan
 * The Object that Controls the Matchmaking section of the 
 * Gomoku game, gets activated by registering or logging into 
 * the game. It sends and receives data as a go-between between
 * the MatchmakingView and the MasterClientController.
 * EthanWiederspan built the basic class outline used in Iteration 1.
 * UPDATE 4/7/18: Updated for Iteration 2: NatalieStephenson. 
 * UPDATE 4/8/18: Initial methods implemented - NatalieStephenson.
 * UPDATE 4/14/18: disconnect() and related functions added - NatalieStephenson
 * UPDATE 4/19/18: comments and documentation polished up - NatalieStephenson
 * UPDATE 4/20/18: Added funcitonality to update chat with info about the lobby
 *
 */
 import java.util.ArrayList; 
public class MatchmakingController {
	
	private MasterClientController masterCon;
	private MatchmakingView matchView;
	ArrayList<String> requestsSent; 
	ArrayList<String> requestsReceived; 
	public static final int HOSTPORT = 5432; 

	/**
	 * constructor for the MatchmakerController
	 * when the MasterClientController creates an instance of
	 * this controller, this controller creates an instance
	 * of the MatchmakerView
	 * 
	 * */
	public MatchmakingController(MasterClientController mCon) {
		requestsReceived = new ArrayList<String>();
		requestsSent = new ArrayList<String>();
		masterCon = mCon;
		matchView = new MatchmakingView(this, masterCon.getUsername());
	}
	
	/**
	 * processMessage is accessed by MasterClientController, which
	 * allows the MatchmakingController to read the tags at the start
	 * of each data stream and take appropriate action depending on whether
	 * the incoming message relayed to it is an invitation or a response
	 * to its invitation, an update on online users, or a user cancellation.
	 * @param data a string consisting of the tag (INVITE or RESPOND tags)
	 * followed immediately by appropriate informaton; formats: INVITE+username
	 * or RESPOND+ConfirmOrDenyTag+username.
	*/
	
	public void processMessage(String data){
		//when i invite, data = INVITEusername tag. 
		//when they invite me, data= INVITEusername
		//when i respond to invite, i data=RESPOND[confirmdeny]username(mine)
		//when they respond to me, data= RESPOND[confirmdeny]username(theirs)
		
		
		if(data.startsWith(Tags.INVITE)){
			this.processRequest(data.substring(Tags.INVITE.length(), data.length())); 			matchView.updateRequests();
		}
		else if(data.startsWith(Tags.RESPOND)){
			this.processResponse(data.substring(Tags.RESPOND.length(), data.length())); 
			matchView.updateRequests();
		}
		else if(data.startsWith(Tags.UPDATEONLINE)){
				this.updateOnlineUsers(data.substring(Tags.UPDATEONLINE.length(), data.length())); 
				matchView.updateRequests();
			}
		else if(data.startsWith(Tags.CANCELINVITE)){
			
			String name = data.substring(Tags.CANCELINVITE.length(), data.length());
			requestsReceived.remove(name); 
			matchView.updateRequests();
		}
		else if (data.startsWith(Tags.FAIL)){
			matchView.updateChat(data.substring(Tags.FAIL.length(),data.length()) + " is currently in-game and cannot play at this time.");
		}
	}
	
	/**
	 * "Do you want to play with me?" 
	 * sendRequest sends an invitation to another user
	 * requesting to play a game with them. It takes the 
	 * username of the desired opponent and relays this back
	 * to the MasterClientController, and through it, the Server.
	 * @param username the string containing the username of the
	 * online user this user wants to play a game with. 
	 * */
	public void sendRequest(String username){
		
		//username
		if(!requestsSent.contains(username)) {
			requestsSent.add(username); 
			
			String s = Tags.INVITE + username; 
			masterCon.sendMessage(s);
		}
		
		matchView.updateChat("Sending challenge to "+username+"...\n");
		
	}
	
	/**
	 * "Look, received RSVP!"
	 * processResponse processes another user's 
	 * response to this user's invitation to play a game. 
	 * If the invited user answers affirmative, then it
	 * opens a Socket in preparation for peer-to-peer gameplay.
	 * @param info, an String array containing the prospective
	 * opponent's response at element 0, and the prospective
	 * opponent's IP address at element 1, if the opponent 
	 * desires to play. 
	 * */
	public void processResponse(String info){
		
		
		//info=[confirmdeny]theirusername
		if(info.startsWith(Tags.DENY)){
			//notify matchView: username denied it...
			matchView.updateChat(info.substring(Tags.DENY.length(), info.length()) + 
								" denied your invite,\n"); 
		
			requestsSent.remove(new String(info.substring(Tags.DENY.length(), info.length())));
		}
		else if(info.startsWith(Tags.CONFIRM)){
			matchView.updateChat(info.substring(Tags.DENY.length(), info.length()) + 
								" accepted your invite!\nStarting game...\n"); 
			
			//notify/prepare: ready to start a game... 
		}
		
		matchView.updateRequests();
		
	}
	
	/**
	 * "I see you want to play with me." 
	 * processRequest commands the MatchmakingView to update
	 * its view to show that a new request to play has arrived. 
	 * @param username a String representing the username of the would-be 
	 * opponent requesting to play with this user.
	 * */
	public void processRequest(String username){
		requestsReceived.add(username); 
		//push username to list
		
		
	}
	
	/**
	 * "Thanks for inviting me! I do/do not want to play."
	 * sendResponse relays this user's response to a received 
	 * invitation to the MasterClientController, which in turn
	 * sends the message over the Server. If this user answers
	 * affirmative, it opens a ServerSocket in preparation for
	 * peer-to-peer gameplay.
	 * @param answer String representing the user's affirmative/
	 * negative response to given invitation.
	 * */
	public void sendResponse(String[] array){
		//Tags.DENY/Tags.CONF is array(0) username is array(1) 
		requestsReceived.remove((array[1]));
	
		String s = Tags.RESPOND + array[0] + array[1];	
		masterCon.sendMessage(s); 
		matchView.updateRequests();
		
		if(array[0].equals(Tags.CONFIRM))    
			masterCon.hostGame(HOSTPORT);
		    
	}
	
	
	/**
	* updateOnlineUsers informs the matchmakingView to update the lobby
	* which users are on or offline.
	* @param data a String consisting of tag followed by username.
	* Tags.ADDUSER/Tags.REMOVEUSERusername
	*
	*/
	public void updateOnlineUsers(String data){
		
	    //remove/add user tag, username
	    if(data.startsWith(Tags.ADDUSER)){
			matchView.updateChat(data.substring(Tags.ADDUSER.length(), data.length())+
								 " logged on.\n");
	        matchView.updateOnline(data.substring(Tags.ADDUSER.length(), data.length())); 
	    }
	    else if(data.startsWith(Tags.REMOVEUSER)){
			matchView.removeOnline(data.substring(Tags.REMOVEUSER.length(), data.length())); 
			matchView.updateChat(data.substring(Tags.REMOVEUSER.length(), data.length())+
								 " logged off.\n");
	    }
	    
	}
	/**
	* cancelRequest sends a message to other users that a previously
	* sent invite is to be canceled. 
	* @param username a String username of the user to notify
	*/
	public void cancelRequest(String username){
		String cancelmsg = Tags.CANCELINVITE + username; 
		masterCon.sendMessage(  cancelmsg  );
	
	}
	
	/**
	* Disconnect function, sends DENY to all requests right before close.
	* Then cancels all requests we have sent in preparation for close.
	* called by the MatchmakingView when the user exits.
	*/
	public void disconnect(){
		
	    //deny all outstanding requests to play
	    for(int i = 0; i<requestsReceived.size(); i++){
	        String[] answer = new String[2]; 
	        answer[0] = Tags.DENY; 
	        answer[1] = requestsReceived.get(i); 
	        
	        this.sendResponse(answer);
	    }
	    //cancel requests made
	    for(int i = 0; i<requestsSent.size(); i++){
	    	this.cancelRequest(requestsSent.get(i)); 
	    }
	    //inform the MasterClientController that we are disconnecting
	    masterCon.disconnect(1);
	}
	

}
