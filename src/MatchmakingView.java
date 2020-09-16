import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The class to create the GUI for the matchmaking.
 * Shows the current online players and allows you to 
 * choose a player to chat with or request a match with.
 * Also gives the option to go to the see the leaderboard.
 * 
 * @author Natalie Stephenson
 * @author Emma Poffenberger
 */
public class MatchmakingView extends JFrame {
	
	private MatchmakingController matchcon;
	private JTextField txtChatWithOnline;
	private JTextField chatMsg;
	private JTextArea chatArea;
	private JList requestList;
	private JList onlinePlayers;
	private DefaultListModel<String> dlmOnline;
	private DefaultListModel<String> dlmRequests;
	private JTextArea top;
	private String username;
	
	
	/**
	 * Constructor for the Matchmaking view, creates
	 * instance of the view and sets up the GUI.
	 * @param matchCon the MatchMakingController the this GUI communicates with
	 */
	public MatchmakingView(MatchmakingController matchCon, String usr ) {
		this.matchcon = matchCon;
		username = usr;
		
		buildView();
		initEventHandlers();

		setTitle("Find an Opponent!");
		setSize(600,550);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		this.setVisible(true);
	}
	
	/**
	 * Constructs the GUI that will be displayed
	 */
	private void buildView() {
		
		
		JPanel panelMain = new JPanel();
		this.getContentPane().add(panelMain);
		panelMain.setLayout(new BorderLayout(0, 0));

		
		//Chat with online players:
		JPanel chatPanel = new JPanel();
		chatPanel.setBackground(SystemColor.control);
		chatPanel.setLayout(new BorderLayout());
		chatPanel.setBorder( new EmptyBorder(5, 5, 5, 5));
		chatPanel.setPreferredSize( new Dimension(250, 450));
		
		txtChatWithOnline = new JTextField();
		txtChatWithOnline.setEditable(false);
		txtChatWithOnline.setBackground(SystemColor.control);
		txtChatWithOnline.setHorizontalAlignment(SwingConstants.CENTER);
		txtChatWithOnline.setText("Chat with Online Players:");
		txtChatWithOnline.setColumns(10);
		txtChatWithOnline.setPreferredSize( new Dimension(500, 25));
		txtChatWithOnline.setBorder( new EmptyBorder(5, 5, 5, 5));
		chatPanel.add(txtChatWithOnline, BorderLayout.NORTH);
		
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setPreferredSize(new Dimension(250, 300));
		chatArea.setBorder(new EmptyBorder(5, 5, 10, 5));
		chatPanel.add(chatArea, BorderLayout.CENTER);

		chatMsg = new JTextField();
		chatMsg.setBackground(SystemColor.info);
		chatMsg.setPreferredSize(new Dimension(200, 25));
		chatMsg.setBorder(new EmptyBorder(5, 5, 10, 5));
		chatPanel.add(chatMsg, BorderLayout.SOUTH);
		
		JScrollBar scrollBar = new JScrollBar();
		chatPanel.add(scrollBar, BorderLayout.EAST);
		
		panelMain.add(chatPanel);
		
		//Tabbed Sections for Online / Requests / LeadershipBoard
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder( new EmptyBorder(5, 5, 5, 5));
		tabbedPane.setPreferredSize(new Dimension(250, 450));
		
		panelMain.add(tabbedPane, BorderLayout.EAST);
		//Online Tab
		JPanel online = new JPanel();
		tabbedPane.addTab("Online Players", null, online, null);
		dlmOnline = new DefaultListModel<String>();
		online.setLayout(null);
		onlinePlayers = new JList<>(dlmOnline);
		onlinePlayers.setBounds(0, 0, 235, 375);
		onlinePlayers.setPreferredSize(new Dimension(250, 375));
		onlinePlayers.setBorder(new EmptyBorder(5, 5, 5, 5));
		online.add(onlinePlayers);
		JButton btnSendRequest = new JButton("Challenge");
		btnSendRequest.setBounds(0, 412, 235, 33);
		btnSendRequest.addActionListener( new ChallengeButtonListener() );
		online.add(btnSendRequest);
		
		//Request Tab
		JPanel requests = new JPanel();
		tabbedPane.addTab("Requests", null, requests, null);
		//DefaultListModel<String> dlmRequests = new DefaultListModel<String>();
		dlmRequests = new DefaultListModel<String>();
		requests.setLayout(null);
		requestList = new JList<>(dlmRequests);
		requestList.setBounds(0, 0, 235, 375);
		requestList.setPreferredSize(new Dimension(250, 375));
		requestList.setBorder(new EmptyBorder(5, 5, 5, 5));
		requests.add(requestList);
		//Accept Request Button
		JButton btnAcceptRequest = new JButton("Accept Request");
		btnAcceptRequest.setBounds(0, 381, 230, 25);
		btnAcceptRequest.setPreferredSize(new Dimension(75, 15));
		btnAcceptRequest.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnAcceptRequest.addActionListener( new AcceptRequestButtonListener() );
		//Deny Request Button
		JButton btnDenyRequest = new JButton("Deny Request" );
		btnDenyRequest.setBounds(0, 410, 230, 25);
		btnDenyRequest.setPreferredSize(new Dimension(75, 15));
		btnDenyRequest.setBorder(new LineBorder(new Color(0, 0, 0)));
		btnDenyRequest.addActionListener( new DenyRequestButtonListener() );
		requests.add(btnAcceptRequest);
		requests.add(btnDenyRequest);
		
		//Leaderboard tab
		JPanel leaderboard = new JPanel();
		tabbedPane.addTab("Leaderboard", null, leaderboard, null);
		
		JTextArea leadershipBoardArea = new JTextArea();
		leadershipBoardArea.setPreferredSize(new Dimension(250, 400));
		leadershipBoardArea.setBorder(new EmptyBorder(5, 5, 5, 5));
		leadershipBoardArea.setEditable(false);
		leaderboard.add(leadershipBoardArea);
		
		top = new JTextArea();
		top.setText("User: " + username );
		top.setBackground(SystemColor.control);
		top.setPreferredSize( new Dimension(500, 20));
		panelMain.add(top, BorderLayout.NORTH);
		
	}
	
	/**
	 * 
	 * @param msg
	 */
	public void updateChat( String msg ) {
		chatArea.append(msg);
	}
	
	/**
	 * Adds a new list element 
	 * @param request
	 */
	public void updateRequests() {		
		dlmRequests.clear();
		for(String r: matchcon.requestsReceived) {
			dlmRequests.addElement( r );
		}
	}
	
	/**
	 * 
	 * @param online
	 */
	public void updateOnline( String online ) {
		dlmOnline.addElement( online );
	}
	
	/**
	 * 
	 * @param online
	 */
	public void removeOnline( String online ) {
		for( int i = 0; i < dlmOnline.getSize(); i++ ) {
			if( online.equalsIgnoreCase(dlmOnline.elementAt(i)) ) {
				dlmOnline.remove(i);
			}
		}
	}

	/**
	 * 
	 *
	 */
	private class ChallengeButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			//make sure they've selected another user
			if(onlinePlayers.getSelectedValue() != null)
				matchcon.sendRequest( onlinePlayers.getSelectedValue().toString() );
		}
	}
	
	/**
	 * 
	 *
	 */
	private class AcceptRequestButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			//make sure they've selected a request
			if(requestList.getSelectedValue() != null) {
				String[] requestA = new String[2];
				requestA[0] = Tags.CONFIRM;
				requestA[1] = requestList.getSelectedValue().toString();
				//requestList.remove( requestList.getSelectedIndex() );
				matchcon.sendResponse( requestA );
			}
			
		}
	}

	/**
	 * 
	 *
	 */
	private class DenyRequestButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			//make sure they've selected a request
			if(requestList.getSelectedValue() != null) {
				String[] requestD = new String[2];
				requestD[0] = Tags.DENY;
				requestD[1] = requestList.getSelectedValue().toString();
				//requestList.remove( requestList.getSelectedIndex() );
				matchcon.sendResponse( requestD );
			}
		}
	}

	/**
	 * sets up the Event Handlers for the interactive 
	 * elements of the GUI.
	 */
	private void initEventHandlers() {
		
		//Exiting
		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				exitProcedure();
			}
		});
	}
	
	// on close send message to controller with close tag (?)
		/**
		 * Procedure when frame is closed, disconnects from server and closes the frame
		 */
		protected void exitProcedure() {
			
			//try {
				matchcon.disconnect();
			//}catch(Exception e){
				//System.out.println("error? " + e.getMessage());
			//}

			System.exit(0);
		}// exitProcedure
}
