package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import Controller.UserController;
import Model.CollaborationRequest;

public class RequestStatusView extends JPanel {
    // Colors
    private static final Color PRIMARY_COLOR = new Color(77, 100, 255);
    private static final Color BACKGROUND_COLOR = new Color(247, 248, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(37, 38, 43);
    private static final Color BORDER_COLOR = new Color(233, 234, 236);
    private static final Color COMPLETED_COLOR = new Color(52, 199, 89);
    private static final Color PENDING_COLOR = new Color(255, 149, 0);
    private static final Color DECLINED_COLOR = new Color(255, 59, 48);
    
    private int currentUserId;
    private JPanel requestsPanel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private List<CollaborationRequest> allRequests;
    
    public RequestStatusView(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create header panel with search and filter
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create requests panel
        requestsPanel = new JPanel();
        requestsPanel.setLayout(new BoxLayout(requestsPanel, BoxLayout.Y_AXIS));
        requestsPanel.setBackground(BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(requestsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BACKGROUND_COLOR);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load initial requests
        loadRequests();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        // Title
        JLabel titleLabel = new JLabel("Request Status");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search, filter and refresh panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(BACKGROUND_COLOR);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, true);
        refreshButton.addActionListener(e -> loadRequests());
        controlPanel.add(refreshButton);
        
        // Status filter
        statusFilter = new JComboBox<>(new String[]{"All", "Accepted", "Declined"});
        statusFilter.setPreferredSize(new Dimension(120, 35));
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilter.addActionListener(e -> filterRequests());
        controlPanel.add(statusFilter);
        
        // Search field
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(5, 10, 5, 10)));
        controlPanel.add(searchField);
        
        headerPanel.add(controlPanel, BorderLayout.EAST);
        
        return headerPanel;
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
        button.setPreferredSize(new Dimension(100, 35));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
    
    private void loadRequests() {
        // Get all requests (both sent and received)
        allRequests = UserController.getAllRequests(currentUserId);
        filterRequests();
    }
    
    private void filterRequests() {
        requestsPanel.removeAll();
        
        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        
        List<CollaborationRequest> filteredRequests = allRequests.stream()
            .filter(request -> {
                boolean matchesSearch = request.getOtherUserName().toLowerCase().contains(searchText);
                boolean matchesStatus = selectedStatus.equals("All") || 
                                     request.getStatus().equalsIgnoreCase(selectedStatus);
                return matchesSearch && matchesStatus;
            })
            .collect(Collectors.toList());
        
        // Update count label
        JLabel countLabel = (JLabel) ((JPanel) ((JPanel) getComponent(0)).getComponent(0)).getComponent(1);
        if (filteredRequests.size() == allRequests.size()) {
            countLabel.setText(allRequests.size() + " requests");
        } else {
            countLabel.setText(filteredRequests.size() + " of " + allRequests.size() + " requests");
        }
        
        if (filteredRequests.isEmpty()) {
            JLabel emptyLabel = new JLabel("No requests found.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(128, 128, 128));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            requestsPanel.add(emptyLabel);
        } else {
            for (CollaborationRequest request : filteredRequests) {
                JPanel requestCard = createRequestCard(request);
                requestCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                requestsPanel.add(requestCard);
                requestsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        requestsPanel.revalidate();
        requestsPanel.repaint();
    }
    
    private JPanel createRequestCard(CollaborationRequest request) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR),
                new EmptyBorder(10, 15, 10, 15)));
        
        // Request info
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
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "accepted":
                return COMPLETED_COLOR;
            case "pending":
                return PENDING_COLOR;
            case "declined":
                return DECLINED_COLOR;
            default:
                return TEXT_COLOR;
        }
    }
    
    private String formatDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        return sdf.format(timestamp);
    }
} 