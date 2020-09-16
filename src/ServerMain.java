/**
 * This main method only starts the view of the server. The server begins
 * running once the "Start Server" button on the ServerView is clicked.
 * Running this main method will bring up the GUI to make that possible.
 * 
 * To begin, enter the port number into the text field and click the Start 
 * button. This class takes no command line arguments.
 * 
 * @author Emily Shane
 */
public class ServerMain {
    public static void main(String[] args) {
        new ServerView();
    }
}