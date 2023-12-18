package student_productivity;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Activity extends JFrame {
    JTextArea displayArea = new JTextArea();
    JLabel wlc = new JLabel("Activity Log");
    JLabel nameJLabel = new JLabel();
    JLabel workh = new JLabel();
    JButton back = new JButton("Back");
    private String user;

    public Activity(String user) {
        this.setTitle("Productivity++ - Activity Log");
        this.setBounds(300, 60, 700, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLayout(null);
        this.user = user;

        readWorkingHoursFromFile();

        Initialize();
        addActionEvent();
    }

    private void readWorkingHoursFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("D:\\hours.txt"))) {
            StringBuilder workingHoursText = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                workingHoursText.append(line).append("\n");
            }

            displayArea.setText(workingHoursText.toString());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error reading working hours from file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void Initialize() {
        wlc.setBounds(170, 0, 350, 100);
        wlc.setForeground(Color.black);
        wlc.setFont(new Font("Dosis SemiBold", Font.BOLD, 50));
        wlc.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(wlc);

        nameJLabel.setText("User's Record:");
        nameJLabel.setBounds(120, 120, 400, 100);
        nameJLabel.setForeground(Color.BLACK);
        nameJLabel.setFont(new Font("Dosis SemiBold", Font.BOLD, 25));
        this.add(nameJLabel);

        workh.setText("Your total working hours: ");
        workh.setBounds(120, 230, 400, 100);
        workh.setForeground(Color.BLACK);
        workh.setFont(new Font("Dosis SemiBold", Font.BOLD, 25));
        this.add(workh);

        displayArea.setBounds(50, 350, 600, 150);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Dosis SemiBold", Font.PLAIN, 16));
        this.add(displayArea);

        back.setBounds(305, 510, 78, 40);
        back.setBorderPainted(false);
        back.setBackground(Color.RED);
        back.setForeground(new Color(159, 89, 155));
        back.setFont(new Font("San Francisco", Font.BOLD, 14));
        this.add(back);
    }

    private void addActionEvent() {
        back.addActionListener(e -> {
            this.setVisible(false);
            // Assuming you have a class named HomePage for navigating back
            HomePage homePage = new HomePage(user);
            homePage.setVisible(true);
        });
    }

    public static void main(String[] args) {
        // For testing the Activity class
        SwingUtilities.invokeLater(() -> {
            Activity activity = new Activity("TestUser");
            activity.setVisible(true);
        });
    }
}
