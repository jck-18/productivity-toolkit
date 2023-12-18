package student_productivity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import javax.imageio.ImageIO;

public class TT extends JFrame {

    private JLabel imageLabel;
    private String imagePath;
    private String user;// Store the path to the image

    public TT(String username1) {
        // Set up the main frame
       
        this.user = username1;
    
        setTitle("TIME TABLE");
        setSize(400, 300);
        setLayout(new FlowLayout());
        
        JPanel homepanel = new JPanel(new GridLayout(4, 2, 10, 5));

        // Create components
        JButton uploadButton = new JButton("Upload New Time-Table");
        JButton showImageButton = new JButton("View Time-Table");
        JLabel idLabel = new JLabel("User ID : :"+username1);

        // Create label for displaying image
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(300, 200));

        // Add components to the frame
        homepanel.add(idLabel);
        homepanel.add(uploadButton);
        homepanel.add(showImageButton);
        //homepanel.add(imageLabel);
        
        
        add(homepanel , BorderLayout.NORTH);

        // Add action listeners to the buttons
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadImage();
            }
        });

        showImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showImage();
            }
        });

        // Load the image path from the properties file
        loadImagePath();
        // Display the image if a path is stored
        if (imagePath != null) {
            displayImage(imagePath);
        }
    }

    private void uploadImage() {
        // Open a file chooser dialog for the user to select an image
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Store the selected image path
            imagePath = selectedFile.getAbsolutePath();
            // Save the image path to the properties file
            saveImagePath(imagePath);

            // Load the selected image and set it as the icon
            displayImage(imagePath);
        }
    }

    private void showImage() {
        // Get the current image icon from the label
        Icon icon = imageLabel.getIcon();

        // Check if an image is already loaded
        if (icon != null) {
            // Open a new frame to display the image at its original resolution
            ImageDisplayFrame displayFrame = new ImageDisplayFrame(icon);
            displayFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "No Time-Table found please uplaod a new one", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveImagePath(String imagePath1) {
        try (OutputStream output = new FileOutputStream(getConfigFilePath())) {
            Properties prop = new Properties();
            prop.setProperty("imagePath", imagePath);
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
        try {
            String url = "jdbc:mysql://localhost:3306/javap";
            String user = "root";
            String pass = "0000";
            Connection connection = DriverManager.getConnection(url, user, pass);
            String sql = "INSERT INTO timetable (username, timetable_image_path) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, this.user);
            preparedStatement.setString(2, imagePath1);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Handle database exception
        }
    }

    private void loadImagePath() {
        try (InputStream input = new FileInputStream(getConfigFilePath())) {
            Properties prop = new Properties();

            // Load a properties file
            prop.load(input);

            // Get the property value and set the imagePath variable
            imagePath = prop.getProperty("imagePath");
        } catch (IOException ex) {
            // It's okay if the file doesn't exist initially
        }
        
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            try {
                ImageIcon icon = new ImageIcon(ImageIO.read(new File(imagePath)));
                imageLabel.setIcon(icon);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading the time table", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getConfigFilePath() {
        // Use the user's home directory for the configuration file
        Path userHomeDir = Paths.get(System.getProperty("user.home"));
        return userHomeDir.resolve("imageframeconfig.properties").toString();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TT("jck").setVisible(true));
    }
}
    

    
        
    


class ImageDisplayFrame extends JFrame {

    public ImageDisplayFrame(Icon icon) {
        setTitle("TIME TABLE");
        setSize(800, 600);
        

        JLabel imageLabel = new JLabel(icon);
        JScrollPane scrollPane = new JScrollPane(imageLabel);

        add(scrollPane);
    }
}