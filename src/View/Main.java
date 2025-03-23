package View;

import javax.swing.SwingUtilities;

/**
 * Main entry point for the Task Management Application.
 * This class initializes the application and launches the user interface.
 */
public class Main {
    /**
     * The main method that starts the application.
     * Uses SwingUtilities.invokeLater to ensure GUI creation happens on the Event Dispatch Thread.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a listener that opens the login view when a user is selected
                UserSelectionViewListener listener = username -> {
                    new LoginView(username).setVisible(true);
                };
                // Start with the user selection view
                new UserSelectionView(listener).setVisible(true);
            } catch (Exception e) {
                System.err.println("Error starting application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

/**
 * Interface for handling user selection events in the UserSelectionView.
 */
interface UserSelectionViewListener {
    /**
     * Called when a user is selected.
     *
     * @param username The username of the selected user
     */
    void onUserSelected(String username);
} 