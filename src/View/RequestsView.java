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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Controller.UserController;
import Model.CollaborationRequest;

public class RequestsView extends JPanel {
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
    private JPanel pendingRequestsPanel;
    private JTextField searchField;
    private List<CollaborationRequest> allPendingRequests;
    
    public RequestsView(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create header panel with title and search
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Pending requests section
        pendingRequestsPanel = new JPanel();
        pendingRequestsPanel.setLayout(new BoxLayout(pendingRequestsPanel, BoxLayout.Y_AXIS));
        pendingRequestsPanel.setBackground(BACKGROUND_COLOR);
        JScrollPane pendingScrollPane = new JScrollPane(pendingRequestsPanel);
        pendingScrollPane.setBorder(BorderFactory.createEmptyBorder());
        pendingScrollPane.setBackground(BACKGROUND_COLOR);
        add(pendingScrollPane, BorderLayout.CENTER);
        
        // Load requests
        loadRequests();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(0, 0, 15, 0)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Pending Requests");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(BACKGROUND_COLOR);
        
        // Search field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search by user...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterRequests(); }
            public void removeUpdate(DocumentEvent e) { filterRequests(); }
            public void insertUpdate(DocumentEvent e) { filterRequests(); }
        });
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadRequests() {
        // Clear panels
        pendingRequestsPanel.removeAll();
        
        // Load pending requests
        allPendingRequests = UserController.getPendingRequests(currentUserId);
        filterRequests();
    }
    
    private void filterRequests() {
        pendingRequestsPanel.removeAll();
        
        String searchText = searchField.getText().toLowerCase();
        
        List<CollaborationRequest> filteredRequests = allPendingRequests.stream()
            .filter(request -> request.getOtherUserName().toLowerCase().contains(searchText))
            .collect(Collectors.toList());
        
        if (filteredRequests.isEmpty()) {
            JLabel emptyLabel = new JLabel("No pending requests found.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(128, 128, 128));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            pendingRequestsPanel.add(emptyLabel);
        } else {
            for (CollaborationRequest request : filteredRequests) {
                JPanel requestCard = createRequestCard(request);
                requestCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                pendingRequestsPanel.add(requestCard);
                pendingRequestsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        pendingRequestsPanel.revalidate();
        pendingRequestsPanel.repaint();
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
    
    private void respondToRequest(CollaborationRequest request, String status) {
        if (UserController.respondToRequest(request.getRequestId(), status)) {
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unable to " + status + " the request.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatDate(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        return sdf.format(timestamp);
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
    }
} 