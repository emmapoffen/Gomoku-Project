import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class makes the view for the server. It is brought up upon running the
 * ServerMain main method. When the "Start Server" button is pressed, the
 * a new ServerModel is created and becomes ready for connections. When the
 * "Stop Server" button is pressed, it initiates the ServerModel's shutdown
 * procedure.
 * 
 * @author Emily Shane
*/
public class ServerView extends JFrame {
	JFrame frame;
	JTextArea connectionInfo;
	JButton startButton;
	JButton stopButton;
	StartListener startListener;
	StopListener stopListener;
	JPanel fullPanel;
	int port;
	ServerModel model;
	JTextField portField;

    /**
     * Constructor for ServerView
     */
	public ServerView(){
		//System.out.println("making view");
		frame = new JFrame();

		//set frame properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(500,400));
		frame.setTitle("Server View");

		fullPanel = new JPanel();
		connectionInfo = new JTextArea();

		//start button to start server processes
		startButton = new JButton("Start Server");
		startListener = new StartListener();
		startButton.addActionListener(startListener);

		//stop button to stop server processes
		stopButton = new JButton("Stop Server");
		stopListener = new StopListener();
		stopButton.addActionListener(stopListener);
		
		//field for port
		portField = new JTextField("54321");
		

		//set up the place for server information to show up
		JScrollPane scroller = new JScrollPane(connectionInfo,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setPreferredSize(new Dimension(450,300));
		connectionInfo.setEditable(false);

		//add components to panel
		fullPanel.add(scroller);
		fullPanel.add(startButton);
		fullPanel.add(stopButton);
		fullPanel.add(portField);

		frame.add(fullPanel);
		frame.pack();
		frame.setVisible(true);
	}
    
    /**
     * Adds to the view information about incoming connections
     */
	public void connectionInfo(String s){
		connectionInfo.append(s);
	}

    /**
     * Takes the input from the port text field of the GUI and uses it to
     * create an instance of a ServerModel. This method is called when the
     * Start button is pressed
     */
	private void startServer(){
		port = Integer.parseInt(portField.getText());
		model = new ServerModel(port, this);
	}

    /**
     * Stops the server when the Stop button is pressed
     * (calls ServerModel's stopServer method)
     */
	private void stopServer(){
	    //clear connectionInfo
		model.stopServer();
	}

	/**
	 * This class is a button listener for the start button
	 */
	private class StartListener implements ActionListener {

		/**
		 * Starts the Server
		 */
		@Override
		public void actionPerformed(ActionEvent e){
			startServer();
		}
	}

	/**
	 * This class is a button listener for the start button
	 */
	private class StopListener implements ActionListener {

		/**
		 * Stops the Server
		 */
		@Override
		public void actionPerformed(ActionEvent e){
			stopServer();
		}
	}
}
