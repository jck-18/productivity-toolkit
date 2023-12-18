package student_productivity;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DynamicGradeTracker extends JFrame {

    private final Map<String, JTextField> subjectFields;
    private final Map<String, Integer> subjectGrades;

    public DynamicGradeTracker() {
        subjectFields = new HashMap<>();
        subjectGrades = new HashMap<>();
        initSubjects();

        // Set up the GUI
        initComponents();
    }

    private void initSubjects() {
        // Ask the user for the number of subjects
        int numSubjects = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of subjects:"));

        // Get the names of the subjects
        for (int i = 0; i < numSubjects; i++) {
            String subjectName = JOptionPane.showInputDialog("Enter the name of subject " + (i + 1) + ":");
            subjectGrades.put(subjectName, 0); // Initialize grades to 0

            // Create and store the JTextField for each subject
            JTextField gradeField = new JTextField("0");
            subjectFields.put(subjectName, gradeField);
        }
    }

    private void initComponents() {
        setTitle("Grade Tracker");
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);

        // Create components
        JPanel panel = new JPanel(new GridLayout(subjectGrades.size() + 6, 2));
        JLabel subjectLabel = new JLabel("Subject");
        JLabel gradeLabel = new JLabel("Marks");
        panel.add(subjectLabel);
        panel.add(gradeLabel);

        for (Map.Entry<String, Integer> entry : subjectGrades.entrySet()) {
            JLabel subject = new JLabel(entry.getKey());
            JTextField gradeField = subjectFields.get(entry.getKey());

            panel.add(subject);
            panel.add(gradeField);
        }

        JButton predictButton = new JButton("Calculate");
        JTextField predictionField = new JTextField();
        predictionField.setEditable(false);

        panel.add(predictButton);
        panel.add(predictionField);

        JLabel gpaLabel = new JLabel("Current GPA:");
        JTextField gpaField = new JTextField();
        gpaField.setEditable(false);
        panel.add(gpaLabel);
        panel.add(gpaField);

        JLabel predictedGpaLabel = new JLabel("Predicted GPA:");
        JTextField predictedGpaField = new JTextField();
        predictedGpaField.setEditable(false);
        panel.add(predictedGpaLabel);
        panel.add(predictedGpaField);

        JLabel cgpaLabel = new JLabel("Predicted CGPA:");
        JTextField cgpaField = new JTextField();
        cgpaField.setEditable(false);
        panel.add(cgpaLabel);
        panel.add(cgpaField);

        // Add action listener for the predict button
        predictButton.addActionListener((ActionEvent e) -> {
            // Update the GUI with entered grades
            for (Map.Entry<String, Integer> entry : subjectGrades.entrySet()) {
                JTextField gradeField = subjectFields.get(entry.getKey());
                entry.setValue(Integer.parseInt(gradeField.getText()));
            }

            // Calculate and display GPA for the current semester
            double totalGradePoints = 0;
            int totalCredits = subjectGrades.size();

            for (Map.Entry<String, Integer> entry : subjectGrades.entrySet()) {
                totalGradePoints += calculateGradePoints(entry.getValue());
            }

            double gpa = totalGradePoints / totalCredits;
            gpaField.setText(String.format("%.2f", gpa));

            // Predict next semester grades and display
            StringBuilder predictionText = new StringBuilder("Next Semester Grades:\n");
            double predictedTotalGradePoints = 0;

            Random random = new Random();

            for (Map.Entry<String, Integer> entry : subjectGrades.entrySet()) {
                int currentGrade = entry.getValue();
                int predictedGrade = currentGrade + random.nextInt(10) - 5; // Random change between -5 and +5
                predictionText.append(entry.getKey()).append(": ").append(predictedGrade).append("\n");
                predictedTotalGradePoints += calculateGradePoints(predictedGrade);
            }

            double predictedGpa = predictedTotalGradePoints / totalCredits;
            predictionField.setText(predictionText.toString());
            predictedGpaField.setText(String.format("%.2f", predictedGpa));

            // Calculate and display CGPA
            double cgpa = calculateCumulativeGPA(gpa, predictedGpa);
            cgpaField.setText(String.format("%.2f", cgpa));
        });

        // Add components to the frame
        add(panel);

        // Display the frame
        setVisible(true);
    }

    private double calculateGradePoints(int grade) {
        // Define a simple GPA scale and assign grade points
        if (grade >= 90) return 10.0;
        else if (grade >= 80) return 9.0;
        else if (grade >= 70) return 8.0;
        else if (grade >= 60) return 7.0;
        else return 0.0; // Fail
    }

    private double calculateCumulativeGPA(double currentGPA, double predictedGPA) {
        // You can implement your CGPA calculation logic here
        // For simplicity, let's assume CGPA is the average of current GPA and predicted GPA
        return (currentGPA + predictedGPA) / 2.0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DynamicGradeTracker());
    }
}
