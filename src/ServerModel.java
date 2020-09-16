import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * ServerModel keeps track of the information for each specific run
 * of the program.
 * This information includes a list of current connections and a list of all registered users.
 * ServerModel also has the responsibility of checking the records in order
 * to authenticate users (check to see if username is taken when registering, 
 * check to see if the password is correct when logging in). ServerModel can
 * also look up a particular ClientConnection by username in order to send
 * them a specific message.
 * 
 * @author Emily Shane
 * @author Ethan Wiederspan
 * 
 * UPDATE 4/23/18(EMW): Added Anonymous name generation and fixed authentication 
 *        to tell user if a player is still online.
 */
public class ServerModel {
	private ArrayList<ClientConnection> connections;
	private ArrayList<User> allUsers;
	private ArrayList<String> animals;
	private ArrayList<String> colors;
	private ServerController control;
	private int portNumber;
	private ServerView servView;

	public ServerModel(int port, ServerView view){
		this.servView = view;
		portNumber = port;
		connections = new ArrayList<>();
		allUsers = loadUsers();
		loadAnon();
		control = new ServerController(this);
		//start the thread in ServerController that listens for new connections
		control.startListening(); 
	}

	/**
	 * Loads the components of the Anon names to be generated.  
	 * Creates a list of animals and a list of Colors which are 
	 * combined to make a new username.
	 */
	private void loadAnon() {
		animals = new ArrayList<String>();
		colors = new ArrayList<String>();
		File animFile = new File("animals.txt");
		File colFile = new File("colors.txt");
		Scanner fileScan;
		try {
			// load animals
			fileScan = new Scanner(animFile);
			while(fileScan.hasNextLine()){
				animals.add(fileScan.nextLine());
			}
			// load colors
			fileScan = new Scanner(colFile);
			while(fileScan.hasNextLine()){
				colors.add(fileScan.nextLine());
			}
			
			fileScan.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		
		
	}

	/**
	 * Adds a new ClientConnection to the ArrayList of active 
	 * ClientConnections and adds the info to the GUI
	 * @param c the new ClientConnection to add to the list
	 */
	public void newConnection(ClientConnection c, String info){
		servView.connectionInfo(info+"\n");
		connections.add(c);
	}


	/**
	 * get method for the port number (which was originally passed
	 * in as a command line argument)
	 * @return the port number
	 */
	public int getPortNumber(){
		return portNumber;
	}

	/**
	 * get method for the list of active ClientConnections
	 * @return the ArrayList of active ClientConnections
	 */
	public ArrayList<ClientConnection> getList(){
		return connections;
	}

	/**
	 * Called by the ServerController upon login; checks to see if the provided username
	 * is in existence in this ServerModel's records. If not, error message is
	 * relayed to user. If username is found, the password is checked against
	 * the one on file for that username. If they match, the User is set to
	 * online and the ClientConnection is assigned that User; the client code
	 * is sent a success message. If password is incorrect, error message is
	 * sent to client code
	 * @return the message for the ClientConnection to send to the client code
	 */
	public String authenticate(String user, String pass, ClientConnection con){

		boolean found = false;
		int i = 0;
		User temp;
		
		System.out.println(allUsers.size());
		if(allUsers.size() == 0) {
			return Tags.UN_NOT_FOUND;
		}
		while(!found && i < allUsers.size()){
			temp = allUsers.get(i);
			if(temp.getUN().equals(user)){
				//normal games don't allow you to be online in multiple places
				//if we want to implement that, do a !temp.isOnline()
				if(temp.getPass() != null && temp.getPass().equals(pass) && !temp.isOnline()){
					found = true;
					temp.setOnline(true);
					con.setUser(temp);
					return Tags.SUCCESS;
				}else if(temp.isOnline()) {
					return Tags.UPDATEONLINE;
				}else{
					found = true;
					return Tags.WRONG_PASS;
				}//If password incorrect
			}//If username found
			else{
				i++;
			}//If not correct username
		}
		return Tags.UN_NOT_FOUND;

	}

	/**
	 * Called by the ServerController; registers a new user. If the username
	 * is already associated with an account, an error message is sent back
	 * to the client. If the username is good, a new User is created and added
	 * to the list of all users, this new User is set to online and added to 
	 * the file of non-anonymous Users, and a success message is sent off to 
	 * the client via the ServerController/ClientConnection
	 * @return the String message for the ClientConnection to pass on to the
	 * user
	 */
	public String register(String user, String pass, ClientConnection con){

		boolean found = false;
		int i = 0;
		User temp;
		while(!found && i < allUsers.size()){
			temp = allUsers.get(i);
			if(temp.getUN().equals(user)){
				found = true;
				return Tags.UN_TAKEN;
			}
			i++;
		}
		
		// Successful Registration, add them as an online use and bring them to Matchmaking
		User add = new User(user,pass,"0");
		add.setOnline(true);
		con.setUser(add);
		allUsers.add(add);
		writeFile(add);

		return Tags.SUCCESS;
	}

	/**
	 * Called by the ServerController; registers a new anonymous user with
	 * a randomly and assigns that User to its ClientConnection
	 * @return the success message
	 */
	public String newAnon(ClientConnection con){
		boolean done = false;
		String anonName = "";
		
		while(!done) {
			// Try to make a new name, if it is taken, try again.
			anonName = this.generateAnonName();
			if(lookUp(anonName) == null) {
				User add = new User(anonName);
				add.setOnline(true);
				con.setUser(add);
				allUsers.add(add);
				done = true;
			}else {
				// Stay in the loop till we make a valid username
			}
		}
		
		return (Tags.AUTH_FEEDBACK + Tags.SUCCESS + anonName);
	}

	/**
	 * Writes a new User to the file of User info
	 * @param newUser the User whose info to write to the file of User info
	 */
	private void writeFile(User newUser){
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter("users.txt", true));
			bw.write(newUser.getUN() + "\n");
			bw.write(newUser.getPass() + "\n");
			bw.write(newUser.getWins() + "\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {     
			if (bw != null) try {
				bw.close();
			} catch (IOException e2) {
				// just ignore it
			}
		}

	}

	/**
	 * This method is called upon instantiation of the ServerModel to load from
	 * the file a list of already registered Users
	 * @return an ArrayList of Users that have already registered in a previous
	 * run of the server
	 */
	private ArrayList<User> loadUsers(){
		ArrayList<User> list = new ArrayList<User>();
		File file = new File("users.txt");
		Scanner fileScan;
		try {
			fileScan = new Scanner(file);
			User temp;
			while(fileScan.hasNextLine()){
				temp = new User(fileScan.nextLine(), fileScan.nextLine(), fileScan.nextLine());
				list.add(temp);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Stops the server's processes; disconnects all ClientConnections and calls
	 * the ServerController's stopServer method
	 */
	public void stopServer(){
		ClientConnection temp;
		for(int i = 0; i < connections.size(); i++){
			temp = connections.get(i);
			disconnect(temp);
		}
		control.stopServer();

	}

	/**
	 * removes the ClientConnection from the list of active connections
	 * @param index the index of the ClientConnection to remove from the list
	 */
	public void removeConnection(int index, String info){
		servView.connectionInfo(info+"\n");
		connections.remove(index);
	}

	/**
	 * Removes the User that is at the given ClientConnection from the list of
	 * Users if it is anonymous, otherwise sets their status as offline. Closes
	 * the socket associated with that ClientConnection.
	 * @param cliCon the ClientConnection to disconnect
	 */
	public void disconnect(ClientConnection cliCon) {
		
        connections.remove(cliCon);
		if(cliCon.getUser() != null) {
			cliCon.getUser().setOnline(false);
			broadcastOnline(cliCon);
			if(cliCon.getUser().getPass() == null) {
				//this means it is anon
				allUsers.remove(cliCon.getUser());
			}
		}
		
		try {
			cliCon.s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Finds the given username from the list of online players.
	 * 
	 * @param target the username of the player to be found
	 * @return the ClientConnection of the found player
	 */
	public ClientConnection lookUp(String target) {

		ClientConnection found = null;
		
		// Go through list of Connections and find user
		for(int i = 0; i < connections.size();i++) {
			if(connections.get(i).getUser() != null && connections.get(i).getUser().getUN().equals(target)) {
				found = connections.get(i);
			}
		}

		// Return ClientConnection only if it was found
		if(found != null && found.getUser().isOnline()) {
			return found;
		}else {
			return null;
		}

	}



	/**
	 * Connects two players to give them reference to each other.  Achieved by
	 * setting peer values of both ClientConnections to the other connection
	 * 
	 * @param con1 the first connection
	 * @param con2 the second connection
	 */
	public void connect(ClientConnection con1, ClientConnection con2) {

		con1.setPeer(con2);
		con2.setPeer(con1);
	}

	/**
	 * Sends a message to this ClientConnection for every user that is
	 *  currently online
	 * 
	 * @param newCon a newly online connection.
	 */
	public void whoseOnline(ClientConnection newCon) {
		
		for(User u: allUsers) {
			if(u.isOnline() && !(u.equals(newCon.getUser())) ) {
				newCon.toClient(Tags.MATCH_FEEDBACK + Tags.UPDATEONLINE + Tags.ADDUSER + u.getUN());
			}
		}
	}

	/**
	 * Tells all other users that this player is online
	 * 
	 * @param newCon the ClientConnection of the player that came online
	 */
	public void broadcastOnline(ClientConnection newCon) {
		
		for(ClientConnection con: connections) {
			if(con.getUser().isOnline() && !(con.equals(newCon)) ) {
				
				if(newCon.getUser().isOnline()) {
					con.toClient(Tags.MATCH_FEEDBACK + Tags.UPDATEONLINE + Tags.ADDUSER + newCon.getUser().getUN());
				}else {
					con.toClient(Tags.MATCH_FEEDBACK + Tags.UPDATEONLINE + Tags.REMOVEUSER + newCon.getUser().getUN());
				}
				
			}
		}
		
	}

	/**
	 * Cancels a request sent, sends the cancel message tag 
	 * and username of player who canceled to the target user
	 * 
	 * @param canceled
	 * @param info
	 */
	public void cancelRequest(ClientConnection canceled, String info) {
		 String target = info.substring(Tags.CANCELINVITE.length(), info.length());
		 
		 ClientConnection targetCon = this.lookUp(target);
		 try {
			 targetCon.toClient(Tags.MATCH_FEEDBACK + Tags.CANCELINVITE + canceled.getUser().getUN());
		 }catch (NullPointerException e) {
			 
		 }
		 
		 
		
	}

	public String generateAnonName() {
		int anmInd = (int) Math.floor(Math.random()*animals.size());
		int clrInd = (int) Math.floor(Math.random()*colors.size());
		
		return colors.get(clrInd)+animals.get(anmInd);
		
	}
}
