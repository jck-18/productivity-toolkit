package student_productivity;
import java.io.FileWriter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class PomoTimer extends JFrame implements ActionListener {
    public static String user;
    public static int count = 0, index = 0, in = 0;
    public static String[] taskstring = new String[100];
    public static String[][] workh = new String[100][2];
    Container container = getContentPane();
    JLabel bgImage = new JLabel(new ImageIcon("C:\\Users\\HP\\Documents\\Pomodoro-App-with-Java-CSE215-main\\src\\Main\\timerbg.png"));
    JLabel time = new JLabel("00:00");
    JLabel c = new JLabel("Pomodoro Count: 0");
    JLabel taskname = new JLabel("");

    JButton back = new JButton("Back");
    JButton startbut = new JButton("Start");
    JButton endbut = new JButton("Mark As Done");

    Timer t;
    Timer q;

    // Add database connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/javap";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "0000";

    public PomoTimer(String user) {
        PomoTimer.user = user;
        this.setTitle("Productivity++ - Pomodoro Timer");
        this.setBounds(300, 60, 700, 600);
        
        this.setResizable(false);
        this.setResizable(false);
        this.setLayout(null);
        this.setIconImage(
                Toolkit.getDefaultToolkit().getImage("C:\\Users\\HP\\Documents\\Pomodoro-App-with-Java-CSE215-main\\src\\Main\\logo.png"));

        loadTasksFromDatabase();

        Initialize();
        addActionEvent();
    }

    private void Initialize() {
        taskname.setText("TASK NAME:" + String.valueOf(taskstring[in]));

        taskname.setBounds(80, 0, 500, 100);
        taskname.setForeground(Color.white);
        taskname.setFont(new Font("Dosis SemiBold", Font.BOLD, 25));
        taskname.setHorizontalAlignment(SwingConstants.CENTER);
        container.add(taskname);

        time.setBounds(185, 80, 300, 300);
        time.setForeground(Color.black);
        time.setFont(new Font("Dosis SemiBold", Font.BOLD, 50));
        time.setHorizontalAlignment(SwingConstants.CENTER);
        container.add(time);

        c.setBounds(185, 140, 300, 300);
        c.setForeground(Color.black);
        c.setFont(new Font("Dosis SemiBold", Font.BOLD, 20));
        c.setHorizontalAlignment(SwingConstants.CENTER);
        container.add(c);

        startbut.setBounds(100, 400, 160, 40);
        startbut.setBorderPainted(false);
        startbut.setBackground(new Color(159, 89, 155));
        startbut.setForeground(Color.WHITE);
        startbut.setFont(new Font("San Francisco", Font.BOLD, 15));
        container.add(startbut);

        endbut.setBounds(420, 400, 160, 40);
        endbut.setBorderPainted(false);
        endbut.setBackground(new Color(159, 89, 155));
        endbut.setForeground(Color.WHITE);
        endbut.setFont(new Font("San Francisco", Font.BOLD, 15));
        container.add(endbut);

        back.setBounds(305, 500, 78, 40);
        back.setBorderPainted(false);
        back.setBackground(Color.WHITE);
        back.setForeground(new Color(159, 89, 155));
        back.setFont(new Font("San Francisco", Font.BOLD, 14));
        container.add(back);

        bgImage.setBounds(-10, -20, 700, 600);
        bgImage.setOpaque(true);
        container.add(bgImage);
    }
   
	

    private void addActionEvent() {
        startbut.addActionListener(this);
        endbut.addActionListener(this);
        back.addActionListener(this);
    }

    int s = 0, h = 0;
    boolean chk = false;

   @Override
public void actionPerformed(ActionEvent e) {
    if (e.getSource() == startbut) {
        chk = true;
        t = new Timer(1, (ActionEvent e1) -> {
            time.setText(String.valueOf(h + "m:" + s + "s"));
            s++;
            
            if (h == 25) {
                t.stop();
                int result = JOptionPane.showConfirmDialog((Component) null,
                        "Take a short Break. Work Done?", "Take A Break", JOptionPane.YES_NO_OPTION);
                
                if (result == 1) {
                    count++;
                    h = 0;
                    s = 0;
                    c.setText("Pomodoro Count: " + count);
                    time.setText(String.valueOf(h + "m:" + s + "s"));
                    // Start a break timer (5 minutes)
                    startBreakTimer();
                } else if (result == 0) {
                    in++;
                    taskname.setText("TASK NAME:" + String.valueOf(taskstring[in]));
                    startPomodoroTimer(count);
                }
            }
            
            if (s == 60) {
                h++;
                s = 0;
            }
        });

        t.start();
    }

    if (e.getSource() == endbut) {
        if (chk == true)
            t.stop();
        in++;

        workhour(count, h);

        taskname.setText("TASK NAME:" + String.valueOf(taskstring[in]));
        s = h = count = 0;
        c.setText("Pomodoro Count: " + count);
        time.setText(String.valueOf(h + "m:" + s + "s"));

        if (taskstring[in] == null) {
            
        }
    }

    if (e.getSource() == back) {
        this.setVisible(false);
        HomePage homePage = new HomePage(user);
        homePage.setVisible(true);
    }
}

private void startBreakTimer() {
    q = new Timer(1, (ActionEvent e) -> {
        time.setText(String.valueOf(h + "m:" + s + "s"));
        s++;
        
        if (h == 5) {
            q.stop();
            
            taskname.setText("TASK NAME:" + String.valueOf(taskstring[in]));
            
            // Inform the user that the break is over
            JOptionPane.showMessageDialog(null, "Break is over. Start next Pomodoro!");
            
            // Increase Pomodoro count
            count++;
            
            // Update UI
            c.setText("Pomodoro Count: " + count);
            
            // Start the next Pomodoro timer for the same task
            startPomodoroTimer(count);
        }
        
        if (s == 60) {
            h++;
            s = 0;
        }
    });

    q.start();
}


private void startPomodoroTimer(int count) {
    s = h = 0;
     
    c.setText("Pomodoro Count: " + count);
    time.setText(String.valueOf(h + "m:" + s + "s"));

    t.start();
}

    private void closeWindow() {
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }

     private void loadTasksFromDatabase() {
    try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
        String query = "SELECT taskname FROM p_timer WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                taskstring[index] = resultSet.getString("taskname");
                index++;
            }
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error loading tasks from database", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
       private void workhour(int count, int hours) {
        // Update the workh array with the working hours for each pomodoro
        workh[count][0] = taskstring[in];
        workh[count][1] = String.valueOf(hours);

        // Save the total working hours to a file
        saveWorkingHoursToFile();
    }

    private void saveWorkingHoursToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\hours.txt"))) {
            for (String[] workh1 : workh) {
                if (workh1[0] != null) {
                    writer.write(workh1[0] + ": " + workh1[1] + " minutes");
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving working hours to file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
     


	

    public static void main(String[] args) {
        if (args.length > 0) {
            user = args[0];
            PomoTimer pomoTimer = new PomoTimer("jck");
            pomoTimer.setVisible(true);
        } else {
            System.out.println("Please provide a username as a command-line argument.");
        }
    }
}