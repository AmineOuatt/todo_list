package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * SidebarPanel class for navigation menu in TaskFrame application
 */
public class SidebarPanel extends JPanel {
    // Modern Microsoft-style colors
    private static final Color PRIMARY_COLOR = new Color(77, 100, 255);    // TickTick Blue
    private static final Color SIDEBAR_COLOR = new Color(247, 248, 250);   // Light Gray
    private static final Color TEXT_COLOR = new Color(37, 38, 43);         // Dark Gray
    private static final Color BORDER_COLOR = new Color(233, 234, 236);    // Light Border
    private static final Color HOVER_COLOR = new Color(242, 243, 245);     // Light Hover

    private JButton pomodoroButton;
    private JButton calendarButton;
    private JButton dashboardButton;
    private JButton tasksButton;
    private JButton notesButton;
    private ActionListener actionListener;

    /**
     * Constructor for SidebarPanel
     * @param actionListener The action listener to handle button clicks
     */
    public SidebarPanel(ActionListener actionListener) {
        this.actionListener = actionListener;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(SIDEBAR_COLOR);
        setBorder(new EmptyBorder(20, 15, 20, 15));

        // Add logo/brand at the top
        JLabel logoLabel = new JLabel("TaskFrame");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(logoLabel);
        add(Box.createVerticalStrut(25));

        // Create navigation options with custom icons 
        String[] navLabels = {"Tasks", "Pomodoro", "Dashboard", "Calendar", "Notes"};
        String[] navActions = {"TASKS", "POMODORO", "DASHBOARD", "CALENDAR", "NOTES"};

        for (int i = 0; i < navLabels.length; i++) {
            JButton button = new JButton(navLabels[i]);
            
            // Add icons later in TaskFrame
            
            styleNavigationButton(button);
            button.setActionCommand(navActions[i]);
            button.addActionListener(actionListener);
            add(button);
            add(Box.createVerticalStrut(5));
            
            // Store references to these buttons for selection highlighting
            if (i == 0) {
                tasksButton = button;
            } else if (i == 1) {
                pomodoroButton = button;
            } else if (i == 2) {
                dashboardButton = button;
            } else if (i == 3) {
                calendarButton = button;
            } else if (i == 4) {
                notesButton = button;
            }
        }

        add(Box.createVerticalGlue());

        // Add separator before logout
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(SIDEBAR_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        add(separator);
        add(Box.createVerticalStrut(15));

        // Add logout button at the bottom
        JButton logoutButton = new JButton();
        logoutButton.setLayout(new BorderLayout(10, 0));
        
        // Icon will be set in TaskFrame
        
        // Create text label
        JLabel textLabel = new JLabel("Logout");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(new Color(255, 89, 89));
        
        logoutButton.add(textLabel, BorderLayout.CENTER);
        styleNavigationButton(logoutButton);
        
        // Override the default style for logout button
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        logoutButton.setActionCommand("LOGOUT");
        logoutButton.addActionListener(actionListener);
        add(logoutButton);
    }

    /**
     * Styles navigation buttons with appropriate look and feel
     * @param button The button to style
     */
    private void styleNavigationButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(SIDEBAR_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
            }
        });
    }

    /**
     * Updates the selected button to highlight it
     * @param actionCommand The action command of the selected button
     */
    public void updateSelectedButton(String actionCommand) {
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getActionCommand().equals(actionCommand)) {
                    button.setBackground(PRIMARY_COLOR);
                    button.setForeground(Color.WHITE);
                } else {
                    button.setBackground(SIDEBAR_COLOR);
                    button.setForeground(TEXT_COLOR);
                }
            }
        }
    }
    
    /**
     * Sets the icon for the button with the given action command
     * @param actionCommand The action command of the button
     * @param icon The icon to set
     */
    public void setButtonIcon(String actionCommand, Icon icon) {
        for (Component component : getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getActionCommand().equals(actionCommand)) {
                    button.setIcon(icon);
                    break;
                }
            }
        }
    }
    
    /**
     * Gets the Pomodoro button
     * @return The Pomodoro button
     */
    public JButton getPomodoroButton() {
        return pomodoroButton;
    }
    
    /**
     * Gets the Calendar button
     * @return The Calendar button
     */
    public JButton getCalendarButton() {
        return calendarButton;
    }
    
    /**
     * Gets the Dashboard button
     * @return The Dashboard button
     */
    public JButton getDashboardButton() {
        return dashboardButton;
    }
    
    /**
     * Gets the Tasks button
     * @return The Tasks button
     */
    public JButton getTasksButton() {
        return tasksButton;
    }
    
    /**
     * Gets the Notes button
     * @return The Notes button
     */
    public JButton getNotesButton() {
        return notesButton;
    }
} 