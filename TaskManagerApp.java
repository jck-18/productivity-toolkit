package student_productivity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import javax.mail.*;
import javax.mail.internet.*;
import org.jdatepicker.JDatePanel;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
// Import the specific Date class from java.util
import java.util.Date;



public class TaskManagerApp extends JFrame {
    private final String username;
    private final DefaultListModel<Task> taskListModel;
    private final JTextField taskDescriptionField;
    private final JComboBox<String> statusComboBox;
    private final JLabel usernameLabel;
    private final JList<Task> taskList;
    private final UtilDateModel dueDateModel;
    private ScheduledExecutorService scheduler;

    public TaskManagerApp(String username) {
        this.username = username;

        // Create the main frame
        JFrame frame = new JFrame("Task Manager");
        JFrame add_task = new JFrame("Add Task");

        frame.setSize(600, 400);
        add_task.setSize(300,200);

        // Display the username at the top
        usernameLabel = new JLabel("Logged in as: " + username);
        frame.add(usernameLabel, BorderLayout.NORTH);



        // Initialize task list model
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new TaskListCellRenderer());
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditDialog();
                }
            }
        });

        JScrollPane taskScrollPane = new JScrollPane(taskList);
        frame.add(taskScrollPane, BorderLayout.CENTER);

        // Create panel for adding tasks
        JPanel addTaskPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        JPanel homepanel = new JPanel (new GridLayout(2,2,5,3));
        taskDescriptionField = new JTextField();
        dueDateModel = new UtilDateModel(); // Initialize the UtilDateModel
        Properties properties = new Properties();
        JDatePanel datePanel = new JDatePanelImpl(dueDateModel, properties);
        JDatePicker datePicker = new JDatePickerImpl((JDatePanelImpl) datePanel, new DateLabelFormatter());
        statusComboBox = new JComboBox<>(new String[]{"Not Completed", "Completed"});
        JButton addButton = new JButton("Add Task");
        JButton saveButton = new JButton("Save");
        JButton deleteCompletedButton = new JButton("Delete Completed Tasks");

        addButton.addActionListener(e -> add_task.setVisible(true));
        saveButton.addActionListener(e -> addTask(add_task));
        deleteCompletedButton.addActionListener(e -> deleteCompletedTasks());

        addTaskPanel.add(new JLabel("Task Description:"));
        addTaskPanel.add(taskDescriptionField);
        addTaskPanel.add(new JLabel("Due Date:"));
        addTaskPanel.add((Component) datePicker); // Add JDatePicker to the panel
        addTaskPanel.add(new JLabel("Status:"));
        addTaskPanel.add(statusComboBox);
        addTaskPanel.add(saveButton);
        homepanel.add(addButton);
        homepanel.add(deleteCompletedButton);

        add_task.add(addTaskPanel, BorderLayout.SOUTH);
        frame.add(homepanel,BorderLayout.SOUTH);
        

        // Set up the frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        add_task.setLocationRelativeTo(null);
        
        

        // Load tasks from the database
        loadTasks();

        
    }
    public void updateTaskListModel(Task oldTask, Task updatedTask) {
        taskListModel.removeElement(oldTask);
        taskListModel.addElement(updatedTask);
    }

    private void showEditDialog() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.", "Edit Task",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        EditTaskDialog editTaskDialog = new EditTaskDialog(this, selectedTask, username, taskListModel);
        editTaskDialog.setVisible(true);

        // Update the task list model if changes were made
        if (editTaskDialog.isChangesMade()) {
            loadTasks();
        }
    }

    void checkAndSendEmailNotification(String username, String taskDescription, Date dueDate) {
        long daysUntilDue = calculateDaysUntilDue(dueDate);

        if (daysUntilDue <= 30){
            sendEmailNotification(username, taskDescription, dueDate, daysUntilDue);
        }
    }

    private long calculateDaysUntilDue(Date dueDate) {
        Calendar today = Calendar.getInstance();
        Calendar due = Calendar.getInstance();
        due.setTime(dueDate);

        long millisecondsPerDay = 24 * 60 * 60 * 1000;
        return ((due.getTimeInMillis() - today.getTimeInMillis()) / millisecondsPerDay) + 1;
    }

    private void sendEmailNotification(String username, String taskDescription, Date dueDate, long daysUntilDue) {
        String userEmail = getUserEmail(username);

        if (userEmail != null && !userEmail.isEmpty()) {
            String subject = "Task Due Soon";
            String message = "Dear " + username + ",\n\n"
                    + "This is a reminder that your task is due soon.\n\n"
                    + "Task Description: " + taskDescription + "\n"
                    + "Due Date: " + dueDate + "\n"
                    + "Days Till Due: " + daysUntilDue  + "\n\n"
                    + "Regards,\nYour Task Manager";

            sendEmail(userEmail, subject, message, "jayantck68@gmail.com", "veae dkix gpie micy");
        }
    }

    private void addTask(JFrame add_task) {
        String taskDescription = taskDescriptionField.getText();
        Date dueDate = dueDateModel.getValue(); // Format the date
        String status = (String) statusComboBox.getSelectedItem();

        // TODO: Add task to the database using JDBC
        try {
            String url = "jdbc:mysql://localhost:3306/javap";
            String user = "root";
            String pass = "0000";
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String sql = "INSERT INTO task_manager (username, task_description, due_date, status) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, taskDescription);
                preparedStatement.setDate(3,new java.sql.Date(dueDate.getTime()));
                preparedStatement.setString(4, status);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            // Handle database exception
            
        }

        // Update the task list model
        Task task = new Task(taskDescription, dueDate, status);
        taskListModel.addElement(task);

        // Clear input fields
        taskDescriptionField.setText("");
        dueDateModel.setValue(null); // Clear the date picker

        // Send email notification
        if("Not Completed".equals(status)){
        checkAndSendEmailNotification(username, taskDescription, dueDate);}

        // Notify the user
        JOptionPane.showMessageDialog(this, "Task added successfully.", "Add Task", JOptionPane.INFORMATION_MESSAGE);
        
        add_task.dispose();
    }

    private void deleteCompletedTasks() {
        // TODO: Delete completed tasks from the database using JDBC
        try {
            String url = "jdbc:mysql://localhost:3306/javap";
            String user = "root";
            String pass = "0000";
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String sql = "DELETE FROM task_manager WHERE username = ? AND status = 'Completed'";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, username);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            // Handle database exception
            
        }

        // Update the task list model by removing completed tasks
        for (int i = taskListModel.size() - 1; i >= 0; i--) {
            Task task = taskListModel.getElementAt(i);
            if ("Completed".equals(task.getStatus())) {
                taskListModel.removeElementAt(i);
            }
        }
    }

    private void loadTasks() {
        taskListModel.clear();
        // TODO: Load tasks from the database using JDBC
        try {
            String url = "jdbc:mysql://localhost:3306/javap";
            String user = "root";
            String pass = "0000";
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String sql = "SELECT task_description, due_date, status FROM task_manager WHERE username = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                while (resultSet.next()) {
                    String taskDescription = resultSet.getString("task_description");
                    Date dueDate = resultSet.getDate("due_date");
                    String status = resultSet.getString("status");
                    Task task = new Task(taskDescription, dueDate , status);
                    taskListModel.addElement(task);
                }
            }
        } catch (SQLException ex) {
            // Handle database exception
            
        }
    }

    private String getUserEmail(String username) {
        // Retrieve the email address associated with the username from the database
        try {
            String url = "jdbc:mysql://localhost:3306/javap";
            String user = "root";
            String pass = "0000";
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String sql = "SELECT email_id FROM user WHERE user_name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    return resultSet.getString("email_id");
                }
            }
        } catch (SQLException ex) {
            // Handle database exception
            
        }

        return null; // Return null if no email is found or an error occurs
    }

    private void sendEmail(String toEmail, String subject, String message, String sendergmailcom, String senderpassword) {
        // Use the existing email sending logic
        // You can customize this method based on your email sending requirements
        // For simplicity, we'll use the previous SMTP settings for Gmail

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        String senderEmail = sendergmailcom;  // Replace with your sender email
        String senderPassword = senderpassword;  // Replace with your sender password

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(senderEmail));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            Transport.send(mimeMessage);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            System.out.println("Failed to send email. Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskManagerApp("jck"));
    }
}

class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private final String datePattern = "yyyy-MM-dd";
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parseObject(text);
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
        }
        return "";
    }
}

class EditTaskDialog extends JDialog {
    private final JTextField editedDescriptionField;
    private final JDatePicker datePicker;
    private JComboBox<String> statusComboBox;
    private final JButton saveButton;
    private final JButton cancelButton;
    private boolean changesMade;
    private final String username;
    private final TaskManagerApp taskManagerApp;
    private final DefaultListModel<Task> taskListModel;
    private final UtilDateModel editedDueDateModel;
    private final JCheckBox statusCheckBox;

    public EditTaskDialog(Frame parent, Task task, String username, DefaultListModel<Task> taskListModel) {
        super(parent, "Edit Task", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        this.username = username;
        this.taskListModel = taskListModel;
        
        this.taskManagerApp = new TaskManagerApp(username);

        editedDescriptionField = new JTextField(task.getTaskDescription());
        editedDueDateModel = new UtilDateModel();
        editedDueDateModel.setValue(task.getDueDate());
        JDatePanel datePanel = new JDatePanelImpl(editedDueDateModel, new Properties());
        datePicker = new JDatePickerImpl((JDatePanelImpl) datePanel, new DateLabelFormatter());

        statusCheckBox = new JCheckBox("Completed", "Completed".equals(task.getStatus()));

        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        saveButton.addActionListener((ActionEvent e) -> {
            saveChanges(task);
            changesMade = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        JPanel editTaskPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        editTaskPanel.add(new JLabel("Edited Description:"));
        editTaskPanel.add(editedDescriptionField);
        editTaskPanel.add(new JLabel("Edited Due Date:"));
        editTaskPanel.add((Component) datePicker);
        editTaskPanel.add(new JLabel("Status:"));
        editTaskPanel.add(statusCheckBox);
        editTaskPanel.add(saveButton);
        editTaskPanel.add(cancelButton);

        add(editTaskPanel, BorderLayout.CENTER);
    }

    public boolean isChangesMade() {
        return changesMade;
    }

    private void saveChanges(Task task) {
        String editedDescription = editedDescriptionField.getText();
        Date editedDueDate = editedDueDateModel.getValue();
        String editedStatus =  (statusCheckBox.isSelected() ? "Completed" : "Not Completed");
        // Update task in the database using JDBC
        try {
            String url = "jdbc:mysql://localhost:3306/javap";
            String user = "root";
            String pass = "0000";
            try (Connection connection = DriverManager.getConnection(url, user, pass)) {
                String sql = "UPDATE task_manager SET task_description = ?, due_date = ?, status = ? WHERE username = ? AND task_description = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, editedDescription);
                preparedStatement.setDate(2, new java.sql.Date(editedDueDate.getTime())); // Convert Date to java.sql.Date
                preparedStatement.setString(3, editedStatus);
                preparedStatement.setString(4, username);
                preparedStatement.setString(5, task.getTaskDescription());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            // Handle database exception
            
        }

        // Remove the old task from the list
        //taskListModel.removeElement(task);

        // Create and add the updated task to the list
        Task updatedTask = new Task(
                editedDescription, 
                editedDueDate, // Get the selected date from JDatePicker
                editedStatus);
        //taskListModel.addElement(updatedTask);
        taskManagerApp.updateTaskListModel(task,updatedTask);
        
        if("Not Completed".equals(editedStatus)){
        taskManagerApp.checkAndSendEmailNotification(username, editedDescription, editedDueDate);
        }
    }
}


class Task {
        private String taskDescription;
    private java.util.Date dueDate;
    private String status;

    public Task(String taskDescription, Date dueDate, String status) {
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return taskDescription + " - Due: " + dueDate + " - Status: " + status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Task otherTask = (Task) obj;
        return Objects.equals(taskDescription, otherTask.taskDescription) &&
                Objects.equals(dueDate, otherTask.dueDate) &&
                Objects.equals(status, otherTask.status);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.taskDescription);
        hash = 79 * hash + Objects.hashCode(this.dueDate);
        hash = 79 * hash + Objects.hashCode(this.status);
        return hash;
    }
}

class TaskListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Task task) {
            setText(task.toString());
        }
        return this;
    }
}
