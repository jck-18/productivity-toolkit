package student_productivity;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteTakingApp extends JFrame {

    private List<Note> notesList;
    private JPanel notesPanel;
    private  JLabel usernameLabel = null;

    // JDBC connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/javap";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "0000";
    private String user;

    public NoteTakingApp(String user) {
        this.user = user;
        setTitle("Note Taking App");
        setSize(600, 400);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        notesList = new ArrayList<>();
        notesPanel = new JPanel();
        usernameLabel = new JLabel("Logged in as: " + user);
        add(usernameLabel, BorderLayout.NORTH);
        notesPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Adjust columns and gaps as needed

        loadNotes();

        JScrollPane scrollPane = new JScrollPane(notesPanel);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Note");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddNoteDialog();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void openAddNoteDialog() {
        NoteDialog noteDialog = new NoteDialog(this, "Add Note", true);
        noteDialog.setVisible(true);
        if (noteDialog.isNoteSaved()) {
            loadNotes();
        }
    }

    private void deleteNoteFromDatabase(String title) {
    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        String sql = "DELETE FROM notes WHERE title = ? AND username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, user);
            preparedStatement.executeUpdate();
            System.out.println("Note deleted from the database: " + title);

            // Remove the deleted note from the notesList
            notesList.removeIf(note -> note.getTitle().equals(title));
        }
    } catch (SQLException ex) {
    }
}

private void openEditNoteDialog(Note note) {
    NoteDialog noteDialog = new NoteDialog(this, "Edit Note", true, note);
    noteDialog.setVisible(true);
    if (noteDialog.isNoteSaved()) {
        // Find and update the existing note in the notesList
        for (Note existingNote : notesList) {
            if (existingNote.getTitle().equals(note.getTitle())) {
                existingNote.setTitle(noteDialog.getUpdatedNote().getTitle());
                existingNote.setContent(noteDialog.getUpdatedNote().getContent());
                break;
            }
        }
        loadNotes();  // This may not be necessary, depending on your requirements
    }
}

    private void loadNotes() {
        List<Note> notes = fetchNotesFromDatabase(user);
        displayNotes(notes);
    }

    private List<Note> fetchNotesFromDatabase(String username) {
        List<Note> notes = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT title, content FROM notes WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        String content = resultSet.getString("content");
                        notes.add(new Note(title, content));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return notes;
    }

    private void displayNotes(List<Note> notes) {
        notesPanel.removeAll();

        for (Note note : notes) {
            NotePanel notePanel = new NotePanel(note);
            notesPanel.add(notePanel);
        }

        notesPanel.revalidate();
        notesPanel.repaint();
    }

    private class NotePanel extends JPanel {
        private Note note;

        public NotePanel(Note note) {
            this.note = note;
            setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel(note.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            add(titleLabel, BorderLayout.NORTH);

            JTextArea contentTextArea = new JTextArea(note.getContent());
            contentTextArea.setEditable(false);
            contentTextArea.setLineWrap(true);
            add(new JScrollPane(contentTextArea), BorderLayout.CENTER);

            JButton editButton = new JButton("Edit");
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openEditNoteDialog(note);
                }
            });

            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteNoteFromDatabase(note.getTitle());
                    loadNotes();
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }
    

    class NoteDialog extends JDialog {

        private JTextField titleField;
        private JTextArea noteTextArea;
        private boolean noteSaved;
        private Note note;

        public NoteDialog(JFrame parent, String title, boolean modal) {
            super(parent, title, modal);
            initUI();
        }

        public NoteDialog(JFrame parent, String title, boolean modal, Note note) {
            super(parent, title, modal);
            this.note = note;
            initUI();
            titleField.setText(note.getTitle());
            noteTextArea.setText(note.getContent());
        }

        private void initUI() {
            setSize(300, 200);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            noteSaved = false;

            titleField = new JTextField();
            noteTextArea = new JTextArea();
            noteTextArea.setFont(new Font("Arial", Font.PLAIN, 14));

            JScrollPane scrollPane = new JScrollPane(noteTextArea);

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {


                    saveNote();
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(saveButton);

            setLayout(new BorderLayout());
            add(titleField, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            setLocationRelativeTo(null);
        }

        private void saveNote() {
            String title = titleField.getText();
            String content = noteTextArea.getText();

            if (note == null) {
                note = new Note(title, content);
            } else {
                note.setTitle(title);
                note.setContent(content);
            }

            saveNoteToDatabase(note, NoteTakingApp.this.user);

            noteSaved = true;

            dispose();
        }

      private void saveNoteToDatabase(Note note, String user) {
    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        String updateNoteQuery = "UPDATE notes SET content = ?, created_date = ? WHERE title = ? AND username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateNoteQuery)) {
            preparedStatement.setString(1, note.getContent());
            preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setString(3, note.getTitle());
            preparedStatement.setString(4, user);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated == 0) {
                // If no rows were updated, the note doesn't exist, so insert it
                String insertNoteQuery = "INSERT INTO notes (title, content, created_date, username) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertNoteQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertStatement.setString(1, note.getTitle());
                    insertStatement.setString(2, note.getContent());
                    insertStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    insertStatement.setString(4, user);
                    insertStatement.executeUpdate();

                    ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int newNoteId = generatedKeys.getInt(1);
                        note.setId(newNoteId);
                    }
                }
            }

            System.out.println("Note saved to the database: " + note.getTitle());
        }
    } catch (SQLException ex) {
    }
}


        public boolean isNoteSaved() {
            return noteSaved;
        }

        public Note getUpdatedNote() {
            return note;
        }
    }

    static class Note {
        private String title;
        private String content;

        public Note(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        private void setId(int newNoteId) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    static class DatabaseNotePanel extends JPanel {
        private Note note;

        public DatabaseNotePanel(Note note) {
            this.note = note;

            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            JLabel titleLabel = new JLabel(note.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setHorizontalAlignment(JLabel.CENTER);

            add(titleLabel, BorderLayout.CENTER);
        }

        public void updateUI(Note updatedNote) {
            note = updatedNote;
            removeAll();

            JLabel titleLabel = new JLabel(note.getTitle());
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setHorizontalAlignment(JLabel.CENTER);

            add(titleLabel, BorderLayout.CENTER);

            revalidate();
            repaint();
        }
    }
}