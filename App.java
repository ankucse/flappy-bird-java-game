import javax.swing.*;

public class App {
    private static int defaultValue;

    public static void main(String[] args) throws Exception {
        int boardWidth = 360;
        int boardHeight = 640;

        // --- Get Game Settings from User ---
        // Ask for the number of players and rounds before starting the game.
        int numPlayers = getNumberFromUser("Enter number of players:");
        int numRounds = getNumberFromUser("Enter number of rounds:");

        // --- Get Player Names ---
        String[] playerNames = new String[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            playerNames[i] = getNameFromUser("Enter name for Player " + (i + 1) + ":");
        }

        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Pass the player names and settings to the game panel constructor.
        FlappyBird flappyBird = new FlappyBird(playerNames, numRounds);
        frame.add(flappyBird);
        frame.pack();
        flappyBird.requestFocus();
        frame.setVisible(true);
    }

    /**
     * A helper method to get a valid positive integer from the user via a dialog box.
     * It includes input validation to ensure a number is entered.
     *
     * @param message The message to display in the dialog.
     * @return The positive integer entered by the user.
     */
    private static int getNumberFromUser(String message) {
        App.defaultValue = 1;
        String input;
        int number;
        while (true) {
            input = JOptionPane.showInputDialog(null, message, "Game Setup", JOptionPane.QUESTION_MESSAGE);
            if (input == null) { // User pressed cancel or closed the dialog
                System.exit(0);
            }
            try {
                number = Integer.parseInt(input);
                if (number > 0) {
                    return number;
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a positive number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * A helper method to get a non-empty name from the user via a dialog box.
     *
     * @param message The message to display in the dialog.
     * @return The name entered by the user.
     */
    private static String getNameFromUser(String message) {
        String name;
        while (true) {
            name = JOptionPane.showInputDialog(null, message, "Player Setup", JOptionPane.QUESTION_MESSAGE);
            if (name == null) { // User pressed cancel or closed the dialog
                System.exit(0);
            }
            name = name.trim(); // Remove leading/trailing whitespace
            if (!name.isEmpty()) {
                return name;
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a name.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}