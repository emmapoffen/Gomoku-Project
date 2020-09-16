import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author EthanWiederspan
 * The class to create the GUI for the Log in screen of the Gomoku 
 * game on the client side.  An instance of this class is created 
 * in LoginController.  This GUI takes in the user's username and
 * password if they are playing online and AI level if they are playing 
 * offline.  It then send data to the LoginController which is processed
 * and sent to the server
 * 
 * @author Emily Shane
 * 4/10/18
 * Updated to implement the use of a Tags class
 *
 */
public class LoginView extends javax.swing.JFrame{

	private LoginController logCon;
	private String gameType = "";	//TO-DO ask about starting gametype
	private String[] gameOptions = {"Select Game Type", "VS. Online Opponent", "VS. Easy AI", 
			"VS. Medium AI", "Vs Hard AI"};

	private JTextArea titleArea;
	private JLabel labelMess;
	private JButton buttonLogin, buttonReg, buttonAnon;
	private JComboBox dropDownGT;
	private JTextField fieldName, fieldPass, fieldPort, fieldServ; 


	/**
	 * Constructor for the LoginView, creates an instance of the 
	 * LoginView and builds and sets up the GUI.  
	 * @param lCon the LoginContoller that is connected to this
	 * 		  LoginView.
	 */
	public LoginView(LoginController lCon) {
		logCon = lCon;

		buildView();
		initEventHandlers();

		setTitle("Gomoku Login");
		setSize(600,200);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.setVisible(true);
	}// Constructor

	/**
	 * Builds the actual GUI that will be displayed to the 
	 * user for Log in.  Includes text fields to enter username
	 * and password as well as the dropdown to choose gametype.
	 */
	private void buildView() {
		JPanel panelMid = new JPanel(new GridBagLayout());
		JPanel panelNorth = new JPanel();
		JPanel panelSouth = new JPanel();

		getContentPane().add(panelNorth, "North");
		getContentPane().add(panelMid);
		getContentPane().add(panelSouth, "South");

		// Build top panel with welcome message
		labelMess = new JLabel("Enter your info to Login/Register OR "
				+ "select AI Game to play offline");
		panelNorth.add(labelMess);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		
		// Set up fields for Server and Port
		JLabel labelServ = new JLabel("Server/port");
		fieldPort = new JTextField(5);
		fieldServ = new JTextField(7);
		
		fieldPort.setText("54321");
		fieldServ.setText("127.0.0.1");

		c.anchor = GridBagConstraints.LINE_START;
		panelMid.add(labelServ, c);
		c.anchor = GridBagConstraints.CENTER;
		panelMid.add(fieldServ, c);
		c.anchor = GridBagConstraints.LINE_END;
		panelMid.add(fieldPort, c);
		
		c.gridy++;

		// Set up fields for username and password
		c.anchor = GridBagConstraints.CENTER;
		fieldName = new JTextField(24);
		fieldPass = new JPasswordField(24);
		fieldName.setText("Username");
		fieldPass.setText("Password");

		panelMid.add(fieldName, c);
		c.gridy++;
		panelMid.add(fieldPass, c);
		c.gridy++;

		// Combo Box to choose GameType
		dropDownGT = new JComboBox(gameOptions);
		panelMid.add(dropDownGT, c);

		// Buttons to start game.
		buttonReg = new JButton("Register");
		buttonLogin = new JButton("Login");
		buttonAnon = new JButton("Play Anonymously");

		panelSouth.add(buttonReg);
		panelSouth.add(buttonLogin);
		panelSouth.add(buttonAnon);
		
	}// buildView

	/**
	 * Sets up the Event Handlers for the buttons 
	 * and dropdown menu of the GUI
	 */
	private void initEventHandlers() {

		// Set up buttons
		ActionListener buttonListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				
				//make sure the info has no spaces.
				if( (isInfoValid()) ) {
					if(e.getActionCommand().equals("Register")) {
						String[] data = {Tags.REGISTER, gameType, fieldName.getText(), fieldPass.getText(), fieldPort.getText(), fieldServ.getText()};
						logCon.authenticate(data);
					}else if(e.getActionCommand().equals("Login")){
						String[] data = {Tags.LOGIN, gameType, fieldName.getText(), fieldPass.getText(), fieldPort.getText(), fieldServ.getText()};
						logCon.authenticate(data);
					}else {
						String[] data = {Tags.ANON, gameType, null, null, fieldPort.getText(), fieldServ.getText()};
						logCon.authenticate(data);
					}
				}else {
					
				}
			}
		};
		buttonReg.addActionListener(buttonListener);
		buttonLogin.addActionListener(buttonListener);
		buttonAnon.addActionListener(buttonListener);		

		// Set up Drop Down
		dropDownGT.addActionListener(
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String gt = (String)dropDownGT.getSelectedItem();


						// If they chose an AI, play a local game
						if(gt.contains("AI")&& !gt.contains("Hard")) {
							gameType = gt;
							logCon.playLocal(gt);
						}
						else if(gt.contains("Hard")) {
							labelMess.setText("Hard level AI not yet implemented");
						}else if(gt.contains("Online")) {
							gameType = gt;
						}

					}
				});

		// Set up close opereation
		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				exitProcedure();
			}
		});

	}// initEventHandlers

	/**
	 * Procedure when frame is closed, disconnects from server and closes the frame
	 */
	protected void exitProcedure() {
		
		try {
			logCon.disconnect();
		}catch(Exception e){
			
		}

		System.exit(0);
	}// exitProcedure

	/*
	 * Tests to see if the username and password are valid inputs,
	 * meaning they aren't null containing no spaces
	 * @return true or false depending on whether or not the inputs are
	 *         valid
	 */
	public boolean isInfoValid() {
		
		String name = fieldName.getText();
		String pass = fieldPass.getText();
		String gt = (String)dropDownGT.getSelectedItem();

		if(name == null || pass == null || pass.contains(" ") || name.contains(" ")){
			updateMessage("Enter a username and password with no spaces");
			return false;
		}else if(gt.contains("AI")) {
			updateMessage("This Game Type is not yet Implemented");
			return false;
		}else if(gt.contains("Select")) {
			updateMessage("Please Select a Game Type");
			return false;
		}
		else {
			return true;
		}

	}// isInfoValid

	/*
	 * Updates the messages displayed at the top of the Login View
	 * @param msg a String containing the message to update the 
	 *         upper label to
	 */
	public void updateMessage(String msg) {
		labelMess.setText(msg);
	}// updateMessages

}
