package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class PomodoroView extends JFrame {
    private TaskFrame parentFrame;
    private Timer pomodoroTimer;
    private int remainingSeconds;
    private boolean isBreak = false;
    private int workDuration = 25 * 60; // 25 minutes in seconds
    private int breakDuration = 5 * 60; // 5 minutes in seconds
    private JLabel timerLabel;
    private JButton startButton;
    private JButton resetButton;
    private CircularProgress progressBar;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(42, 120, 255);
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = new Color(225, 225, 225);
    private static final Color BREAK_COLOR = new Color(255, 152, 0);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);

    public PomodoroView(TaskFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        setTitle("Pomodoro Timer");
        setSize(400, 600);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);

        // Create main container
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add header with back button
        JPanel headerPanel = createHeaderPanel();
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(headerPanel);
        mainContainer.add(Box.createVerticalStrut(20));

        // Timer panel
        JPanel timerPanel = createTimerPanel();
        timerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(timerPanel);
        mainContainer.add(Box.createVerticalStrut(30));

        // Settings panel
        JPanel settingsPanel = createSettingsPanel();
        settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(settingsPanel);

        add(mainContainer);

        // Initialize timer
        initializeTimer();

        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.setVisible(true);
                dispose();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // Back button
        JButton backButton = new JButton("← Back");
        styleButton(backButton, false);
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        // Title
        JLabel titleLabel = new JLabel("Pomodoro Timer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTimerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(30, 30, 30, 30)
        ));

        // Progress circle
        progressBar = new CircularProgress();
        progressBar.setPreferredSize(new Dimension(200, 200));
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.setBackground(CARD_COLOR);
        progressPanel.add(progressBar);
        panel.add(progressPanel);

        // Timer label
        timerLabel = new JLabel("25:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        timerLabel.setForeground(TEXT_COLOR);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(20));
        panel.add(timerLabel);

        // Status label
        JLabel statusLabel = new JLabel("Time to focus!");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusLabel.setForeground(new Color(100, 100, 100));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(statusLabel);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBackground(CARD_COLOR);

        startButton = new JButton("Start");
        resetButton = new JButton("Reset");
        styleButton(startButton, true);
        styleButton(resetButton, false);

        startButton.addActionListener(e -> toggleTimer());
        resetButton.addActionListener(e -> resetTimer());

        buttonsPanel.add(startButton);
        buttonsPanel.add(resetButton);

        panel.add(Box.createVerticalStrut(30));
        panel.add(buttonsPanel);

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        // Work duration setting
        JPanel workPanel = new JPanel(new BorderLayout(10, 0));
        workPanel.setBackground(CARD_COLOR);
        workPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel workLabel = new JLabel("Work Duration (minutes)");
        workLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JSpinner workSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 60, 1));
        workSpinner.setPreferredSize(new Dimension(70, 30));
        styleSpinner(workSpinner);
        workSpinner.addChangeListener(e -> {
            workDuration = (int)workSpinner.getValue() * 60;
            if (!pomodoroTimer.isRunning()) {
                remainingSeconds = workDuration;
                updateDisplay();
            }
        });

        workPanel.add(workLabel, BorderLayout.WEST);
        workPanel.add(workSpinner, BorderLayout.EAST);
        panel.add(workPanel);
        panel.add(Box.createVerticalStrut(10));

        // Break duration setting
        JPanel breakPanel = new JPanel(new BorderLayout(10, 0));
        breakPanel.setBackground(CARD_COLOR);
        breakPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JLabel breakLabel = new JLabel("Break Duration (minutes)");
        breakLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JSpinner breakSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        breakSpinner.setPreferredSize(new Dimension(70, 30));
        styleSpinner(breakSpinner);
        breakSpinner.addChangeListener(e -> breakDuration = (int)breakSpinner.getValue() * 60);

        breakPanel.add(breakLabel, BorderLayout.WEST);
        breakPanel.add(breakSpinner, BorderLayout.EAST);
        panel.add(breakPanel);

        return panel;
    }

    private void styleButton(JButton button, boolean isPrimary) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
        button.setForeground(isPrimary ? Color.WHITE : PRIMARY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPrimary ? PRIMARY_COLOR : BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(new Color(0, 99, 177));
                } else {
                    button.setBackground(new Color(245, 245, 245));
                }
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
            }
        });
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(Color.WHITE);
            tf.setForeground(TEXT_COLOR);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }
    }

    private void initializeTimer() {
        remainingSeconds = workDuration;
        pomodoroTimer = new Timer(1000, e -> updateTimer());
        updateDisplay();
    }

    private void toggleTimer() {
        if (pomodoroTimer.isRunning()) {
            pomodoroTimer.stop();
            startButton.setText("Start");
        } else {
            pomodoroTimer.start();
            startButton.setText("Pause");
        }
    }

    private void resetTimer() {
        pomodoroTimer.stop();
        remainingSeconds = workDuration;
        isBreak = false;
        startButton.setText("Start");
        updateDisplay();
    }

    private void updateTimer() {
        remainingSeconds--;
        if (remainingSeconds <= 0) {
            pomodoroTimer.stop();
            Toolkit.getDefaultToolkit().beep();
            
            if (isBreak) {
                remainingSeconds = workDuration;
                isBreak = false;
                JOptionPane.showMessageDialog(this, "Break is over! Time to work!");
                progressBar.setColor(PRIMARY_COLOR);
            } else {
                remainingSeconds = breakDuration;
                isBreak = true;
                JOptionPane.showMessageDialog(this, "Time for a break!");
                progressBar.setColor(BREAK_COLOR);
            }
            
            startButton.setText("Start");
        }
        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        
        double progress = 1.0 - (remainingSeconds / (double)(isBreak ? breakDuration : workDuration));
        progressBar.setProgress(progress);
        progressBar.repaint();
    }

    // Custom circular progress component
    private class CircularProgress extends JComponent {
        private double progress = 0.0;
        private Color color = PRIMARY_COLOR;
        private static final int STROKE_WIDTH = 10;

        public void setProgress(double progress) {
            this.progress = progress;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height);
            int x = (width - size) / 2;
            int y = (height - size) / 2;

            // Draw background circle
            g2.setColor(new Color(230, 230, 230));
            g2.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x + STROKE_WIDTH/2, y + STROKE_WIDTH/2, 
                       size - STROKE_WIDTH, size - STROKE_WIDTH, 
                       90, -360);

            // Draw progress arc
            g2.setColor(color);
            g2.drawArc(x + STROKE_WIDTH/2, y + STROKE_WIDTH/2, 
                       size - STROKE_WIDTH, size - STROKE_WIDTH, 
                       90, (int)(-360 * progress));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 200);
        }
    }
}