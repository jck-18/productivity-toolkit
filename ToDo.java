package student_productivity;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ToDo extends JFrame implements ActionListener {
    JLabel wlc = new JLabel("");
    Container container = getContentPane();
    JLabel bgImage = new JLabel(new ImageIcon("C:\\Users\\HP\\Documents\\Pomodoro-App-with-Java-CSE215-main\\src\\Main\\homebg.png"));

    JTextArea list = new JTextArea();
    JTextField add = new JTextField();
    JButton addButton = new JButton("ADD");
    JButton back = new JButton("Back");
    JButton deleteButton = new JButton("Delete");

    public static String user;

    // Add database connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/javap";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "0000";

    public ToDo(String user) {
        this.user = user;
        this.setTitle("Productivity++ - ToDo");
        this.setBounds(300, 60, 700, 600);
        
        this.setResizable(false);
        this.setLayout(null);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\HP\\Documents\\Pomodoro-App-with-Java-CSE215-main\\src\\Main\\logo.png"));

        Initialize();
        addActionEvent();
        loadTasksFromDatabase();
    }

    private void Initialize() {
        wlc = new JLabel("Create ToDo List");
        wlc.setBounds(170, 0, 350, 100);
        wlc.setForeground(Color.white);
        wlc.setFont(new Font("Dosis SemiBold", Font.BOLD, 40));
        wlc.setHorizontalAlignment(SwingConstants.CENTER);
        container.add(wlc);

        list.setBounds(45, 100, 600, 300);
        list.setEditable(false);
        list.setBackground(new Color(250, 192, 192));
        list.setFont(new Font("San Francisco", Font.BOLD, 14));
        list.setForeground(Color.BLACK);
        JScrollPane sp = new JScrollPane(list);
        sp.setBounds(45, 100, 600, 300);
        container.add(sp);

        add.setBounds(45, 450, 400, 35);
        container.add(add);

        addButton.setBounds(470, 450, 80, 35);
        addButton.setBorderPainted(false);
        addButton.setBackground(new Color(159, 89, 155));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("San Francisco", Font.BOLD, 15));
        container.add(addButton);

        deleteButton.setBounds(570, 450, 90, 35);
        deleteButton.setBorderPainted(false);
        deleteButton.setBackground(new Color(250, 74, 74));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("San Francisco", Font.BOLD, 15));
        container.add(deleteButton);

        back.setBounds(297, 505, 78, 40);
        back.setBorderPainted(false);
        back.setBackground(Color.WHITE);
        back.setForeground(new Color(159, 89, 155));
        back.setFont(new Font("San Francisco", Font.BOLD, 14));
        container.add(back);

        bgImage.setBounds(0, 0, 700, 600);
        bgImage.setOpaque(true);
        container.add(bgImage);
    }

    private void addActionEvent() {
        addButton.addActionListener(this);
        back.addActionListener(this);
        deleteButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            String task = add.getText();
            insertTaskIntoDatabase(user, task);

            JOptionPane.showMessageDialog(null, "Successfully Added!", "Confirmation", JOptionPane.WARNING_MESSAGE);

            this.setVisible(false);
            ToDo a = new ToDo(user);
            a.setVisible(true);
        }

        if (e.getSource() == back) {
            this.setVisible(false);
            HomePage homePage = new HomePage(user);
            homePage.setVisible(true);
        }

        if (e.getSource() == deleteButton) {
            deleteTasksForUser(user);

            this.setVisible(false);
            ToDo todo = new ToDo(user);
            todo.setVisible(true);
        }
    }

    private void loadTasksFromDatabase() {
    try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
        String query = "SELECT taskname FROM p_timer WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String task = resultSet.getString("taskname"); // Use "taskname" instead of "task"
                list.append("    " + task + "\n");
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error loading tasks from database", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private void insertTaskIntoDatabase(String user, String task) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "INSERT INTO p_timer (username, taskname) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, user);
                preparedStatement.setString(2, task);
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error inserting task into database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTasksForUser(String user) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String query = "DELETE FROM p_timer WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, user);
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting tasks for user from database", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            user = args[0];
            ToDo todo = new ToDo(user);
            todo.setVisible(true);
        } else {
            System.out.println("Please provide a username as a command-line argument.");
        }
    }
}
