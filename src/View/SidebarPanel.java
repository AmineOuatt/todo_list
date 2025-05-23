package View;

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
    private JButton collaborationsButton;
    private ActionListener actionListener;
    private JButton selectedButton; // Track the currently selected button

    /**
     * Constructor for SidebarPanel
     * @param actionListener The action listener to handle button clicks
     */
    public SidebarPanel(ActionListener actionListener) {
        this.actionListener = actionListener;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(SIDEBAR_COLOR);
        setBorder(new EmptyBorder(30, 25, 30, 25));
        setPreferredSize(new Dimension(220, getPreferredSize().height));

        // Add logo/brand at the top
        JLabel logoLabel = new JLabel("Todo List");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(logoLabel);
        add(Box.createVerticalStrut(40));

        // Create navigation options with custom icons 
        String[] navLabels = {"Tasks", "Pomodoro", "Dashboard", "Calendar", "Notes", "Collaborations"};
        String[] navActions = {"TASKS", "POMODORO", "DASHBOARD", "CALENDAR", "NOTES", "COLLABORATIONS"};

        for (int i = 0; i < navLabels.length; i++) {
            JButton button = new JButton(navLabels[i]);
            
            // Add icons later in TaskFrame
            
            styleNavigationButton(button);
            button.setActionCommand(navActions[i]);
            button.addActionListener(actionListener);
            add(button);
            add(Box.createVerticalStrut(12));
            
            // Store references to these buttons for selection highlighting
            if (i == 0) {
                tasksButton = button;
                selectedButton = button; // Default selection is Tasks
            } else if (i == 1) {
                pomodoroButton = button;
            } else if (i == 2) {
                dashboardButton = button;
            } else if (i == 3) {
                calendarButton = button;
            } else if (i == 4) {
                notesButton = button;
            } else if (i == 5) {
                collaborationsButton = button;
            }
        }

        add(Box.createVerticalGlue());

        // Add separator before logout
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(SIDEBAR_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        add(separator);
        add(Box.createVerticalStrut(20));

        // Add logout button at the bottom
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        logoutButton.setForeground(new Color(255, 89, 89));
        styleNavigationButton(logoutButton);
        
        // Override the default style for logout button
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        logoutButton.setIconTextGap(10);
        logoutButton.setActionCommand("LOGOUT");
        logoutButton.addActionListener(actionListener);
        add(logoutButton);
        
        // Set initial selection
        updateSelectedButton("TASKS");
    }

    /**
     * Styles navigation buttons with appropriate look and feel
     * @param button The button to style
     */
    private void styleNavigationButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setForeground(TEXT_COLOR);
        button.setBackground(SIDEBAR_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setIconTextGap(12);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Only show hover effect if this button is not already selected
                if (button != selectedButton) {
                    button.setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Only revert to original color if this button is not selected
                if (button != selectedButton) {
                    button.setBackground(SIDEBAR_COLOR);
                }
            }
        });
    }

    /**
     * Updates the selected button to highlight it
     * @param actionCommand The action command of the selected button
     */
    public void updateSelectedButton(String actionCommand) {
        // First reset the previously selected button if there is one
        if (selectedButton != null) {
            selectedButton.setBackground(SIDEBAR_COLOR);
            selectedButton.setForeground(TEXT_COLOR);
            
            // Special handling for logout button
            if ("LOGOUT".equals(selectedButton.getActionCommand())) {
                selectedButton.setForeground(new Color(255, 89, 89));
            }
        }
        
        // Find and update the newly selected button
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getActionCommand().equals(actionCommand)) {
                    button.setBackground(PRIMARY_COLOR);
                    
                    // Special handling for logout button - keep it red text even when selected
                    if ("LOGOUT".equals(actionCommand)) {
                        button.setForeground(new Color(255, 89, 89));
                    } else {
                        button.setForeground(Color.WHITE);
                    }
                    
                    selectedButton = button; // Keep track of the selected button
                    break;
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

    /**
     * Gets the Collaborations button
     * @return The Collaborations button
     */
    public JButton getCollaborationsButton() {
        return collaborationsButton;
    }
} 