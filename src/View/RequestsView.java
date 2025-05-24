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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
    private JPanel sentRequestsPanel;
    
    public RequestsView(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create tabbed pane for pending and sent requests
        JPanel tabbedPane = new JPanel();
        tabbedPane.setLayout(new BoxLayout(tabbedPane, BoxLayout.Y_AXIS));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        
        // Pending requests section
        JLabel pendingTitle = new JLabel("Pending Requests");
        pendingTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pendingTitle.setForeground(TEXT_COLOR);
        pendingTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabbedPane.add(pendingTitle);
        tabbedPane.add(Box.createVerticalStrut(15));
        
        pendingRequestsPanel = new JPanel();
        pendingRequestsPanel.setLayout(new BoxLayout(pendingRequestsPanel, BoxLayout.Y_AXIS));
        pendingRequestsPanel.setBackground(BACKGROUND_COLOR);
        JScrollPane pendingScrollPane = new JScrollPane(pendingRequestsPanel);
        pendingScrollPane.setBorder(BorderFactory.createEmptyBorder());
        pendingScrollPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.add(pendingScrollPane);
        
        // Sent requests section
        tabbedPane.add(Box.createVerticalStrut(20));
        JLabel sentTitle = new JLabel("Sent Requests");
        sentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sentTitle.setForeground(TEXT_COLOR);
        sentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabbedPane.add(sentTitle);
        tabbedPane.add(Box.createVerticalStrut(15));
        
        sentRequestsPanel = new JPanel();
        sentRequestsPanel.setLayout(new BoxLayout(sentRequestsPanel, BoxLayout.Y_AXIS));
        sentRequestsPanel.setBackground(BACKGROUND_COLOR);
        JScrollPane sentScrollPane = new JScrollPane(sentRequestsPanel);
        sentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        sentScrollPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.add(sentScrollPane);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Load requests
        loadRequests();
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
                return DECLINED_COLOR;
            default:
                return TEXT_COLOR;
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