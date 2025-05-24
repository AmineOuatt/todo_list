package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Controller.SubTaskController;
import Controller.TaskController;
import Controller.UserController;
import Model.CollaborationRequest;
import Model.SubTask;
import Model.Task;
import Model.User;

public class CollaborationView extends JPanel {
    // Couleurs
    private static final Color PRIMARY_COLOR = new Color(77, 100, 255);
    private static final Color BACKGROUND_COLOR = new Color(247, 248, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(37, 38, 43);
    private static final Color BORDER_COLOR = new Color(233, 234, 236);
    private static final Color COMPLETED_COLOR = new Color(52, 199, 89);
    private static final Color PENDING_COLOR = new Color(255, 149, 0);
    private static final Color IN_PROGRESS_COLOR = new Color(255, 149, 0);
    
    private int currentUserId;
    private JPanel collaboratorsPanel;
    private JPanel statisticsPanel;
    private JTextField searchField;
    private JList<User> searchResults;
    private DefaultListModel<User> searchResultsModel;
    private JPanel pendingRequestsPanel;
    private JPanel sentRequestsPanel;

    public CollaborationView(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // En-tête
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Panneau principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Panneau de gauche: liste des collaborateurs
        collaboratorsPanel = createCollaboratorsPanel();
        JScrollPane collaboratorsScrollPane = new JScrollPane(collaboratorsPanel);
        collaboratorsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        collaboratorsScrollPane.setBackground(BACKGROUND_COLOR);
        
        // Panneau de droite: statistiques
        statisticsPanel = createStatisticsPanel();
        JScrollPane statisticsScrollPane = new JScrollPane(statisticsPanel);
        statisticsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        statisticsScrollPane.setBackground(BACKGROUND_COLOR);
        
        // Divise l'écran en deux parties
        mainPanel.add(collaboratorsScrollPane, BorderLayout.WEST);
        mainPanel.add(statisticsScrollPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        loadCollaborators();
        loadRequests();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Collaborations");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(BACKGROUND_COLOR);
        
        // Search field
        searchField = new JTextField();
        styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.setMaximumSize(new Dimension(300, 40));
        searchField.putClientProperty("JTextField.placeholderText", "Search for a user...");
        
        // Search results
        searchResultsModel = new DefaultListModel<>();
        searchResults = new JList<>(searchResultsModel);
        searchResults.setCellRenderer(new UserListCellRenderer());
        JScrollPane searchResultsScroll = new JScrollPane(searchResults);
        searchResultsScroll.setVisible(false);
        
        // Add collaborator button
        JButton addButton = new JButton("Send Request");
        styleButton(addButton, true);
        
        // Panel to contain search field and button
        JPanel searchInputPanel = new JPanel(new BorderLayout());
        searchInputPanel.setBackground(BACKGROUND_COLOR);
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(addButton, BorderLayout.EAST);
        
        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
        searchPanel.add(searchResultsScroll, BorderLayout.CENTER);
        
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        // Handle events
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchUsers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchUsers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchUsers();
            }
            
            private void searchUsers() {
                String query = searchField.getText().trim();
                if (query.isEmpty()) {
                    searchResultsModel.clear();
                    searchResultsScroll.setVisible(false);
                } else {
                    List<User> users = UserController.searchUsersByUsername(query);
                    searchResultsModel.clear();
                    for (User user : users) {
                        if (user.getUserId() != currentUserId) { // Don't add current user
                            searchResultsModel.addElement(user);
                        }
                    }
                    searchResultsScroll.setVisible(true);
                }
                revalidate();
                repaint();
            }
        });
        
        // Send collaboration request
        addButton.addActionListener(e -> {
            User selectedUser = searchResults.getSelectedValue();
            if (selectedUser != null) {
                sendCollaborationRequest(selectedUser);
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Please select a user.", 
                        "No user selected", 
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        
        return headerPanel;
    }
    
    private void sendCollaborationRequest(User user) {
        if (UserController.sendCollaborationRequest(currentUserId, user.getUserId())) {
            JOptionPane.showMessageDialog(this, 
                    "Collaboration request sent to " + user.getUsername(), 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            searchField.setText("");
            loadCollaborators();
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Unable to send collaboration request to " + user.getUsername(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createCollaboratorsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 0, 20));
        
        // Create tabbed pane for collaborators and requests
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        
        // Collaborators tab
        JPanel collaboratorsTab = new JPanel();
        collaboratorsTab.setLayout(new BoxLayout(collaboratorsTab, BoxLayout.Y_AXIS));
        collaboratorsTab.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("My collaborators");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        collaboratorsTab.add(titleLabel);
        collaboratorsTab.add(Box.createVerticalStrut(15));
        
        collaboratorsPanel = new JPanel();
        collaboratorsPanel.setLayout(new BoxLayout(collaboratorsPanel, BoxLayout.Y_AXIS));
        collaboratorsPanel.setBackground(BACKGROUND_COLOR);
        collaboratorsTab.add(collaboratorsPanel);
        
        // Requests tab
        JPanel requestsTab = new JPanel();
        requestsTab.setLayout(new BoxLayout(requestsTab, BoxLayout.Y_AXIS));
        requestsTab.setBackground(BACKGROUND_COLOR);
        
        // Pending requests section
        JLabel pendingTitle = new JLabel("Pending Requests");
        pendingTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pendingTitle.setForeground(TEXT_COLOR);
        pendingTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        requestsTab.add(pendingTitle);
        requestsTab.add(Box.createVerticalStrut(15));
        
        JPanel pendingRequestsPanel = new JPanel();
        pendingRequestsPanel.setLayout(new BoxLayout(pendingRequestsPanel, BoxLayout.Y_AXIS));
        pendingRequestsPanel.setBackground(BACKGROUND_COLOR);
        requestsTab.add(pendingRequestsPanel);
        
        // Sent requests section
        requestsTab.add(Box.createVerticalStrut(20));
        JLabel sentTitle = new JLabel("Sent Requests");
        sentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sentTitle.setForeground(TEXT_COLOR);
        sentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        requestsTab.add(sentTitle);
        requestsTab.add(Box.createVerticalStrut(15));
        
        JPanel sentRequestsPanel = new JPanel();
        sentRequestsPanel.setLayout(new BoxLayout(sentRequestsPanel, BoxLayout.Y_AXIS));
        sentRequestsPanel.setBackground(BACKGROUND_COLOR);
        requestsTab.add(sentRequestsPanel);
        
        // Add tabs
        tabbedPane.addTab("Collaborators", collaboratorsTab);
        tabbedPane.addTab("Requests", requestsTab);
        
        panel.add(tabbedPane);
        
        // Store references to panels
        this.pendingRequestsPanel = pendingRequestsPanel;
        this.sentRequestsPanel = sentRequestsPanel;
        
        return panel;
    }
    
    private void loadCollaborators() {
        // Clear collaborators panel
        collaboratorsPanel.removeAll();
        
        JLabel titleLabel = new JLabel("My collaborators");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        collaboratorsPanel.add(titleLabel);
        collaboratorsPanel.add(Box.createVerticalStrut(15));
        
        List<User> collaborators = UserController.getCollaborators(currentUserId);
        
        if (collaborators.isEmpty()) {
            JLabel emptyLabel = new JLabel("You don't have any collaborators yet.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(128, 128, 128));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            collaboratorsPanel.add(emptyLabel);
        } else {
            for (User collaborator : collaborators) {
                JPanel collaboratorCard = createCollaboratorCard(collaborator);
                collaboratorCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                collaboratorsPanel.add(collaboratorCard);
                collaboratorsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        // Update statistics
        updateStatistics(collaborators);
        
        collaboratorsPanel.revalidate();
        collaboratorsPanel.repaint();
    }
    
    private JPanel createCollaboratorCard(User collaborator) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)));
        card.setMaximumSize(new Dimension(300, 80));
        card.setPreferredSize(new Dimension(300, 80));
        
        // Avatar (initiale)
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight()) - 10;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Couleur aléatoire basée sur le nom d'utilisateur
                Color avatarColor = getRandomColor(collaborator.getUsername());
                g2d.setColor(avatarColor);
                g2d.fillOval(x, y, size, size);
                
                // Initiale
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2d.getFontMetrics();
                String initial = collaborator.getUsername().substring(0, 1).toUpperCase();
                int textX = x + (size - fm.stringWidth(initial)) / 2;
                int textY = y + ((size - (fm.getAscent() + fm.getDescent())) / 2) + fm.getAscent();
                g2d.drawString(initial, textX, textY);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }
        };
        avatarPanel.setOpaque(false);
        
        // Informations
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_COLOR);
        infoPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        JLabel nameLabel = new JLabel(collaborator.getUsername());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        
        infoPanel.add(nameLabel);
        
        // Bouton supprimer
        JButton removeButton = new JButton("Supprimer");
        removeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        removeButton.setForeground(new Color(255, 59, 48));
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setFocusPainted(false);
        removeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        removeButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous vraiment supprimer la collaboration avec " + collaborator.getUsername() + " ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                if (UserController.removeCollaboration(currentUserId, collaborator.getUserId())) {
                    loadCollaborators();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Impossible de supprimer la collaboration.",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        card.add(avatarPanel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(removeButton, BorderLayout.EAST);
        
        return card;
    }
    
    private Color getRandomColor(String seed) {
        int hash = seed.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        // Assurer une couleur assez vive
        r = Math.max(r, 100);
        g = Math.max(g, 100);
        b = Math.max(b, 100);
        return new Color(r, g, b);
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Progress Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        JLabel helpText = new JLabel("Add collaborators to see statistics.");
        helpText.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        helpText.setForeground(new Color(128, 128, 128));
        helpText.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(helpText);
        
        return panel;
    }
    
    private void updateStatistics(List<User> collaborators) {
        // Clear statistics panel
        statisticsPanel.removeAll();
        
        JLabel titleLabel = new JLabel("Progress Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statisticsPanel.add(titleLabel);
        statisticsPanel.add(Box.createVerticalStrut(15));
        
        // If no collaborators yet
        if (collaborators.isEmpty()) {
            JLabel emptyLabel = new JLabel("Add collaborators to view statistics");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(128, 128, 128));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            statisticsPanel.add(emptyLabel);
            return;
        }
        
        // Get statistics for all users (including current user)
        Map<Integer, UserStats> userStatsMap = new HashMap<>();
        
        // Get current user info
        User currentUser = UserController.getUserById(currentUserId);
        if (currentUser != null) {
            UserStats currentUserStats = new UserStats(currentUser.getUsername());
            userStatsMap.put(currentUserId, currentUserStats);
            
            // Add collaborators' stats
            for (User collaborator : collaborators) {
                UserStats collaboratorStats = new UserStats(collaborator.getUsername());
                userStatsMap.put(collaborator.getUserId(), collaboratorStats);
            }
            
            // Calculate task statistics for current user
            List<Task> currentUserTasks = TaskController.getTasksWithOccurrences(currentUserId);
            for (Task task : currentUserTasks) {
                // Only count tasks owned by current user
                if (task.getUserId() == currentUserId) {
                    updateTaskStats(userStatsMap.get(currentUserId), task);
                }
            }
            
            // Calculate task statistics for collaborators
            for (User collaborator : collaborators) {
                List<Task> collaboratorTasks = TaskController.getTasksWithOccurrences(collaborator.getUserId());
                for (Task task : collaboratorTasks) {
                    // Only count tasks owned by this collaborator
                    if (task.getUserId() == collaborator.getUserId()) {
                        updateTaskStats(userStatsMap.get(collaborator.getUserId()), task);
                    }
                }
            }
            
            // Generate UI components for statistics
            
            // Task Completion Progress - Bar charts
            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
            progressPanel.setBackground(CARD_COLOR);
            progressPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR),
                    new EmptyBorder(15, 15, 15, 15)));
            
            JLabel progressTitle = new JLabel("Task Completion Progress");
            progressTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            progressTitle.setForeground(TEXT_COLOR);
            progressPanel.add(progressTitle);
            progressPanel.add(Box.createVerticalStrut(10));
            
            // Current user progress
            UserStats currentStats = userStatsMap.get(currentUserId);
            JPanel currentUserRow = createProgressRow(
                    currentStats.username, 
                    currentStats.completedTasks, 
                    currentStats.totalTasks,
                    true);
            progressPanel.add(currentUserRow);
            progressPanel.add(Box.createVerticalStrut(10));
            
            // Collaborators progress
            for (User collaborator : collaborators) {
                UserStats stats = userStatsMap.get(collaborator.getUserId());
                if (stats != null) {
                    JPanel collaboratorRow = createProgressRow(
                            stats.username,
                            stats.completedTasks,
                            stats.totalTasks,
                            false);
                    progressPanel.add(collaboratorRow);
                    progressPanel.add(Box.createVerticalStrut(10));
                }
            }
            
            progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, progressPanel.getPreferredSize().height));
            progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            statisticsPanel.add(progressPanel);
            statisticsPanel.add(Box.createVerticalStrut(20));
            
            // Task Status Distribution - Pie charts
            JPanel pieChartsTitle = new JPanel(new BorderLayout());
            pieChartsTitle.setBackground(BACKGROUND_COLOR);
            pieChartsTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
            
            JLabel pieTitle = new JLabel("Task Status Distribution");
            pieTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            pieTitle.setForeground(TEXT_COLOR);
            pieChartsTitle.add(pieTitle, BorderLayout.WEST);
            pieChartsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            statisticsPanel.add(pieChartsTitle);
            
            JPanel pieChartsPanel = new JPanel(new GridLayout(0, Math.min(collaborators.size() + 1, 3), 15, 15));
            pieChartsPanel.setBackground(BACKGROUND_COLOR);
            
            // Current user pie chart
            JPanel currentUserPie = createPieChartPanel(
                    currentStats.username,
                    currentStats.completedTasks,
                    currentStats.inProgressTasks,
                    currentStats.pendingTasks);
            pieChartsPanel.add(currentUserPie);
            
            // Collaborators pie charts
            for (User collaborator : collaborators) {
                UserStats stats = userStatsMap.get(collaborator.getUserId());
                if (stats != null) {
                    JPanel collaboratorPie = createPieChartPanel(
                            stats.username,
                            stats.completedTasks,
                            stats.inProgressTasks,
                            stats.pendingTasks);
                    pieChartsPanel.add(collaboratorPie);
                }
            }
            
            pieChartsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            statisticsPanel.add(pieChartsPanel);
        }
        
        statisticsPanel.revalidate();
        statisticsPanel.repaint();
    }
    
    private void updateTaskStats(UserStats stats, Task task) {
        stats.totalTasks++;
        
        // Increment appropriate counter based on status
        String status = task.getStatus().toLowerCase();
        if (status.contains("completed")) {
            stats.completedTasks++;
        } else if (status.contains("progress")) {
            stats.inProgressTasks++;
        } else {
            stats.pendingTasks++;
        }
        
        // Check subtasks
        List<SubTask> subTasks = SubTaskController.getSubTasksByTaskId(task.getTaskId());
        if (subTasks != null) {
            for (SubTask subTask : subTasks) {
                stats.totalSubtasks++;
                if (subTask.isCompleted()) {
                    stats.completedSubtasks++;
                }
            }
        }
    }
    
    private JPanel createProgressRow(String username, int completed, int total, boolean isCurrentUser) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        
        JLabel nameLabel = new JLabel(username + (isCurrentUser ? " (You)" : ""));
        nameLabel.setFont(new Font("Segoe UI", isCurrentUser ? Font.BOLD : Font.PLAIN, 14));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setPreferredSize(new Dimension(150, 20));
        
        JProgressBar progressBar = new JProgressBar(0, Math.max(1, total));
        progressBar.setValue(completed);
        progressBar.setStringPainted(true);
        progressBar.setForeground(isCurrentUser ? PRIMARY_COLOR : COMPLETED_COLOR);
        progressBar.setString(completed + "/" + total);
        
        JLabel percentLabel = new JLabel(total > 0 ? Math.round((float)completed / total * 100) + "%" : "0%");
        percentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        percentLabel.setForeground(TEXT_COLOR);
        percentLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(percentLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createPieChartPanel(String username, int completed, int inProgress, int pending) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        
        JLabel nameLabel = new JLabel(username, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        
        int total = completed + inProgress + pending;
        
        PieChartPanel pieChart = new PieChartPanel(completed, inProgress, pending);
        pieChart.setPreferredSize(new Dimension(150, 150));
        
        JPanel legendPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        legendPanel.setBackground(CARD_COLOR);
        
        legendPanel.add(createLegendItem("Completed", COMPLETED_COLOR, completed, total));
        legendPanel.add(createLegendItem("In Progress", IN_PROGRESS_COLOR, inProgress, total));
        legendPanel.add(createLegendItem("Pending", Color.LIGHT_GRAY, pending, total));
        
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(pieChart, BorderLayout.CENTER);
        panel.add(legendPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLegendItem(String label, Color color, int value, int total) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(CARD_COLOR);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(12, 12));
        
        int percentage = total > 0 ? (int)(((float)value / total) * 100) : 0;
        JLabel textLabel = new JLabel(label + " " + percentage + "%");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textLabel.setForeground(TEXT_COLOR);
        
        panel.add(colorBox);
        panel.add(textLabel);
        
        return panel;
    }
    
    private class PieChartPanel extends JPanel {
        private int completed;
        private int inProgress;
        private int pending;
        
        public PieChartPanel(int completed, int inProgress, int pending) {
            this.completed = completed;
            this.inProgress = inProgress;
            this.pending = pending;
            setBackground(CARD_COLOR);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height) - 20;
            int x = (width - size) / 2;
            int y = (height - size) / 2;
            
            int total = completed + inProgress + pending;
            
            if (total == 0) {
                // If no tasks, draw empty circle
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillOval(x, y, size, size);
            } else {
                // Draw pie chart
                int startAngle = 0;
                
                // Segment for completed tasks
                int arcAngle = (int) Math.round(360.0 * completed / total);
                g2d.setColor(COMPLETED_COLOR);
                g2d.fillArc(x, y, size, size, startAngle, arcAngle);
                startAngle += arcAngle;
                
                // Segment for in-progress tasks
                arcAngle = (int) Math.round(360.0 * inProgress / total);
                g2d.setColor(IN_PROGRESS_COLOR);
                g2d.fillArc(x, y, size, size, startAngle, arcAngle);
                startAngle += arcAngle;
                
                // Segment for pending tasks
                arcAngle = 360 - startAngle; // Take the rest to avoid rounding errors
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillArc(x, y, size, size, startAngle, arcAngle);
            }
        }
    }
    
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }
    
    private void styleButton(JButton button, boolean isPrimary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (isPrimary) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(TEXT_COLOR);
            button.setBorder(new LineBorder(BORDER_COLOR, 1));
        }
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 40));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isPrimary) {
                    // Ensure the RGB values don't exceed 255
                    int r = Math.min(255, PRIMARY_COLOR.getRed() + 20);
                    int g = Math.min(255, PRIMARY_COLOR.getGreen() + 20);
                    int b = Math.min(255, PRIMARY_COLOR.getBlue() + 20);
                    button.setBackground(new Color(r, g, b));
                } else {
                    button.setBackground(new Color(245, 245, 245));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(PRIMARY_COLOR);
                } else {
                    button.setBackground(Color.WHITE);
                }
            }
        });
    }
    
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof User) {
                User user = (User) value;
                setText(user.getUsername());
            }
            
            return this;
        }
    }
    
    private class UserStats {
        String username;
        int totalTasks;
        int completedTasks;
        int inProgressTasks;
        int pendingTasks;
        int totalSubtasks;
        int completedSubtasks;
        
        public UserStats(String username) {
            this.username = username;
        }
    }
    
    private void loadRequests() {
        // Clear panels
        pendingRequestsPanel.removeAll();
        sentRequestsPanel.removeAll();
        
        // Load pending requests
        List<CollaborationRequest> pendingRequests = UserController.getPendingRequests(currentUserId);
        if (pendingRequests.isEmpty()) {
            JLabel emptyLabel = new JLabel("No pending requests.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(128, 128, 128));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            pendingRequestsPanel.add(emptyLabel);
        } else {
            for (CollaborationRequest request : pendingRequests) {
                JPanel requestCard = createRequestCard(request);
                requestCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                pendingRequestsPanel.add(requestCard);
                pendingRequestsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        // Load sent requests
        List<CollaborationRequest> sentRequests = UserController.getSentRequests(currentUserId);
        if (sentRequests.isEmpty()) {
            JLabel emptyLabel = new JLabel("No sent requests.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(128, 128, 128));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            sentRequestsPanel.add(emptyLabel);
        } else {
            for (CollaborationRequest request : sentRequests) {
                JPanel requestCard = createSentRequestCard(request);
                requestCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                sentRequestsPanel.add(requestCard);
                sentRequestsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        pendingRequestsPanel.revalidate();
        pendingRequestsPanel.repaint();
        sentRequestsPanel.revalidate();
        sentRequestsPanel.repaint();
    }
    
    private JPanel createRequestCard(CollaborationRequest request) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(10, 15, 10, 15)));
        
        // User info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_COLOR);
        
        JLabel nameLabel = new JLabel(request.getOtherUserName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        
        JLabel dateLabel = new JLabel("Sent " + formatDate(request.getCreatedAt()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(128, 128, 128));
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(dateLabel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(CARD_COLOR);
        
        JButton acceptButton = new JButton("Accept");
        styleButton(acceptButton, true);
        acceptButton.addActionListener(e -> respondToRequest(request, "accepted"));
        
        JButton declineButton = new JButton("Decline");
        styleButton(declineButton, false);
        declineButton.addActionListener(e -> respondToRequest(request, "declined"));
        
        buttonPanel.add(acceptButton);
        buttonPanel.add(declineButton);
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private JPanel createSentRequestCard(CollaborationRequest request) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(10, 15, 10, 15)));
        
        // User info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_COLOR);
        
        JLabel nameLabel = new JLabel(request.getOtherUserName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        
        JLabel statusLabel = new JLabel("Status: " + request.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(getStatusColor(request.getStatus()));
        
        JLabel dateLabel = new JLabel("Sent " + formatDate(request.getCreatedAt()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(128, 128, 128));
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(dateLabel);
        
        // Resend button (only for declined requests)
        if (request.isDeclined()) {
            JButton resendButton = new JButton("Resend");
            styleButton(resendButton, true);
            resendButton.addActionListener(e -> resendRequest(request));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(CARD_COLOR);
            buttonPanel.add(resendButton);
            
            card.add(buttonPanel, BorderLayout.EAST);
        }
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void respondToRequest(CollaborationRequest request, String status) {
        if (UserController.respondToRequest(request.getRequestId(), status)) {
            loadCollaborators();
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unable to " + status + " the request.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resendRequest(CollaborationRequest request) {
        if (UserController.resendRequest(request.getRequestId())) {
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unable to resend the request.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "accepted":
                return COMPLETED_COLOR;
            case "pending":
                return PENDING_COLOR;
            case "declined":
                return new Color(255, 59, 48);
            default:
                return TEXT_COLOR;
        }
    }
    
    private String formatDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        return sdf.format(timestamp);
    }
} 