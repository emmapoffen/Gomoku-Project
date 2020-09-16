/**
 * @author Natalie Stephenson
 * This class contains only a main method, which creates an instance of a
 * MasterClientController, which will begin the process of a game of Gomoku by
 * creating a LoginView.
 * 
 * No command line arguments are necessary for this class.
 */

public class Gomoku {

	/**
	 * @param args input from the command line
	 */
	public static void main(String[] args) {
        MasterClientController control = new MasterClientController(args);
	}

}
