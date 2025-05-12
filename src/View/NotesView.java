package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import Controller.CategoryController;
import Controller.NoteController;
import Model.Category;
import Model.Note;

public class NotesView extends JPanel {
    private int userId;
    private JTextField titleField;
    private JTextArea contentArea;
    private JComboBox<Category> categoryComboBox;
    private JButton saveButton, deleteButton, newButton;
    private DefaultListModel<Note> notesListModel;
    private JList<Note> notesList;
    private Note currentNote;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    
    // Colors (matching TaskFrame)
    private static final Color PRIMARY_COLOR = new Color(77, 100, 255);    // TickTick Blue
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255); // White
    private static final Color SIDEBAR_COLOR = new Color(247, 248, 250);   // Light Gray
    private static final Color TEXT_COLOR = new Color(37, 38, 43);         // Dark Gray
    private static final Color BORDER_COLOR = new Color(233, 234, 236);    // Light Border
    private static final Color HOVER_COLOR = new Color(242, 243, 245);     // Light Hover

    public NotesView(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(BACKGROUND_COLOR);
        
        // Create notes list panel (left side)
        JPanel notesListPanel = createNotesListPanel();
        
        // Create note detail panel (right side)
        JPanel noteDetailPanel = createNoteDetailPanel();
        
        // Add panels to main view
        add(notesListPanel, BorderLayout.WEST);
        add(noteDetailPanel, BorderLayout.CENTER);
        
        // Load notes
        loadNotes();
    }
    
    private JPanel createNotesListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setPreferredSize(new Dimension(300, getHeight()));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        
        // Header with title, filter, and add button
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(SIDEBAR_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Title label on the left
        JLabel titleLabel = new JLabel("Notes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_COLOR);
        
        // New button on the right
        newButton = new JButton("+ New");
        newButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newButton.setFocusPainted(false);
        newButton.setBackground(PRIMARY_COLOR);
        newButton.setForeground(Color.WHITE);
        newButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        newButton.addActionListener(e -> createNewNote());
        
        // Panel for title and new button
        JPanel titleButtonPanel = new JPanel(new BorderLayout(10, 0));
        titleButtonPanel.setBackground(SIDEBAR_COLOR);
        titleButtonPanel.add(titleLabel, BorderLayout.WEST);
        titleButtonPanel.add(newButton, BorderLayout.EAST);
        
        headerPanel.add(titleButtonPanel, BorderLayout.NORTH);
        
        // Category filter below the title
        JPanel filterPanel = new JPanel(new BorderLayout(5, 0));
        filterPanel.setBackground(SIDEBAR_COLOR);
        filterPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterPanel.add(filterLabel, BorderLayout.WEST);
        
        JComboBox<Category> filterComboBox = new JComboBox<>();
        // Add "All Categories" option
        filterComboBox.addItem(new Category(0, "All Notes"));
        
        // Load categories
        List<Category> categories = CategoryController.getAllCategories();
        for (Category category : categories) {
            filterComboBox.addItem(category);
        }
        
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterComboBox.addActionListener(e -> {
            Category selectedCategory = (Category) filterComboBox.getSelectedItem();
            if (selectedCategory.getId() == 0) {
                loadNotes(); // Load all notes
            } else {
                loadNotesByCategory(selectedCategory.getId()); // Load notes by category
            }
        });
        
        filterPanel.add(filterComboBox, BorderLayout.CENTER);
        headerPanel.add(filterPanel, BorderLayout.CENTER);
        
        // Notes list
        notesListModel = new DefaultListModel<>();
        notesList = new JList<>(notesListModel);
        notesList.setCellRenderer(new NoteListCellRenderer());
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Note selectedNote = notesList.getSelectedValue();
                if (selectedNote != null) {
                    displayNoteDetails(selectedNote);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(notesList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(SIDEBAR_COLOR);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNoteDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Note detail header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        JLabel headerLabel = new JLabel("Note Details");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Add panel to contain buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        
        saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        saveButton.setFocusPainted(false);
        saveButton.setBackground(PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        saveButton.addActionListener(e -> saveNote());
        saveButton.setEnabled(false);
        
        deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deleteButton.setFocusPainted(false);
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        deleteButton.addActionListener(e -> deleteNote());
        deleteButton.setEnabled(false);
        
        buttonsPanel.add(saveButton);
        buttonsPanel.add(deleteButton);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 10, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Title field
        JLabel titleLabel = new JLabel("Title");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(titleLabel, gbc);
        
        gbc.gridy++;
        titleField = new JTextField();
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(titleField, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 10, 0);
        
        // Category selection
        JLabel categoryLabel = new JLabel("Category");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(categoryLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        categoryComboBox = new JComboBox<>();
        
        // Add "No Category" option
        categoryComboBox.addItem(new Category(0, "No Category"));
        
        // Load categories
        List<Category> categories = CategoryController.getAllCategories();
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
        
        categoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(categoryComboBox, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 10, 0);
        
        // Content area
        JLabel contentLabel = new JLabel("Content");
        contentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(contentLabel, gbc);
        
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentArea = new JTextArea();
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        formPanel.add(contentScroll, gbc);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadNotes() {
        notesListModel.clear();
        List<Note> notes = NoteController.getNotes(userId);
        for (Note note : notes) {
            notesListModel.addElement(note);
        }
        
        // Clear form fields
        clearForm();
    }
    
    private void loadNotesByCategory(int categoryId) {
        notesListModel.clear();
        List<Note> notes = NoteController.getNotesByCategory(userId, categoryId);
        for (Note note : notes) {
            notesListModel.addElement(note);
        }
        
        // Clear form fields
        clearForm();
    }
    
    private void displayNoteDetails(Note note) {
        currentNote = note;
        titleField.setText(note.getTitle());
        contentArea.setText(note.getContent());
        
        // Set the category in the combo box
        if (note.getCategory() != null) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category category = categoryComboBox.getItemAt(i);
                if (category.getId() == note.getCategory().getId()) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            categoryComboBox.setSelectedIndex(0); // No category
        }
        
        // Enable buttons
        saveButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }
    
    private void createNewNote() {
        clearForm();
        currentNote = null;
        saveButton.setEnabled(true);
        deleteButton.setEnabled(false);
    }
    
    private void saveNote() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected category
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        if (selectedCategory.getId() == 0) {
            selectedCategory = null; // No category selected
        }
        
        boolean success;
        
        if (currentNote == null) {
            // Create new note
            if (selectedCategory != null) {
                success = NoteController.createNote(userId, title, content, selectedCategory);
            } else {
                success = NoteController.createNote(userId, title, content);
            }
        } else {
            // Update existing note
            if (selectedCategory != null) {
                success = NoteController.updateNote(currentNote.getNoteId(), title, content, selectedCategory);
            } else {
                success = NoteController.updateNote(currentNote.getNoteId(), title, content);
            }
        }
        
        if (success) {
            loadNotes(); // Refresh the list
            JOptionPane.showMessageDialog(this, "Note saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save note", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteNote() {
        if (currentNote == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this note?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (NoteController.deleteNote(currentNote.getNoteId())) {
                loadNotes(); // Refresh the list
                clearForm();
                JOptionPane.showMessageDialog(this, "Note deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete note", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        titleField.setText("");
        contentArea.setText("");
        categoryComboBox.setSelectedIndex(0);
        currentNote = null;
        saveButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    // Custom cell renderer for notes list
    private class NoteListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            if (isSelected) {
                panel.setBackground(HOVER_COLOR);
            } else {
                panel.setBackground(SIDEBAR_COLOR);
            }
            
            if (value instanceof Note) {
                Note note = (Note) value;
                
                // Title
                JLabel titleLabel = new JLabel(note.getTitle());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(titleLabel);
                
                // Add some space
                panel.add(Box.createVerticalStrut(5));
                
                // Preview of content (first 50 chars)
                String preview = note.getContent();
                if (preview.length() > 50) {
                    preview = preview.substring(0, 47) + "...";
                }
                JLabel previewLabel = new JLabel(preview);
                previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                previewLabel.setForeground(new Color(100, 100, 100));
                previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(previewLabel);
                
                // Add some space
                panel.add(Box.createVerticalStrut(5));
                
                // Category and date
                JPanel metaPanel = new JPanel(new BorderLayout());
                metaPanel.setBackground(panel.getBackground());
                
                String categoryText = "";
                if (note.getCategory() != null) {
                    categoryText = note.getCategory().getName();
                }
                
                JLabel categoryLabel = new JLabel(categoryText);
                categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                categoryLabel.setForeground(PRIMARY_COLOR);
                
                String dateText = dateFormat.format(note.getCreatedDate());
                JLabel dateLabel = new JLabel(dateText);
                dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                dateLabel.setForeground(new Color(150, 150, 150));
                
                metaPanel.add(categoryLabel, BorderLayout.WEST);
                metaPanel.add(dateLabel, BorderLayout.EAST);
                metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                panel.add(metaPanel);
                
                // Add separator
                JSeparator separator = new JSeparator();
                separator.setForeground(BORDER_COLOR);
                separator.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(Box.createVerticalStrut(10));
                panel.add(separator);
            }
            
            return panel;
        }
    }
} 