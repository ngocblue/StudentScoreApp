import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

class Student {
    String name;
    List<Double> scores;
    double averageScore;
    
    public Student(String name, List<Double> scores) {
        this.name = name;
        this.scores = scores;
        this.averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}

public class StudentScoreApp {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<Student> getStudentData() {
        //Create Panel for input fields
    	JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Use BoxLayout
        
        //Input number of students
        JTextField studentField = new JTextField(10); // Set character width
        studentField.setMaximumSize(new Dimension(150, 25)); // Set fixed size
        panel.add(new JLabel("Enter number of students (5-50):"));
        panel.add(studentField);
        
        //Select subjects
        String[] defaultSubjects =  {"Mathematics", "Physics", "Chemistry", "Biology", "Computer Science"};
        JList<String> subjectList = new JList<>(defaultSubjects);
        subjectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(subjectList);
        panel.add(new JLabel("Select Subjects (Hold Ctrl to select multiple):"));
        panel.add(scrollPane);
        
        //Display the first input dialog
        int result = JOptionPane.showConfirmDialog(null, panel, "Student & Subject Input", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) System.exit(0);
        
        int studentCount;
        try {
            studentCount = Integer.parseInt(studentField.getText().trim());
            if (studentCount < 5 || studentCount > 50) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid student number. Must be between 05 and 50.", "Error", JOptionPane.ERROR_MESSAGE);
            return getStudentData();
        }
        
        String[] subjects = subjectList.getSelectedValuesList().toArray(new String[0]); // Convert JList selection to String[]
        if (subjects.length == 0 || subjects[0].isEmpty()) {
            JOptionPane.showMessageDialog(null, "Subjects cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return getStudentData();
        }
        
        //Define column name for input table
        String[] columnNames = new String[subjects.length + 1];
        columnNames[0] = "Name";
        System.arraycopy(subjects, 0, columnNames, 1, subjects.length);
        
        DefaultTableModel model = new DefaultTableModel(studentCount, columnNames.length);
        model.setColumnIdentifiers(columnNames);
        JTable table = new JTable(model);
        
        while (true) {
            JScrollPane scrollPane2 = new JScrollPane(table);
            int submitResult = JOptionPane.showConfirmDialog(null, scrollPane2, "Enter Student Names and Scores", JOptionPane.OK_CANCEL_OPTION);
            if (submitResult != JOptionPane.OK_OPTION) System.exit(0);
            
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            
            List<Student> students = new ArrayList<>();
            boolean validInput = true;
            
            for (int i = 0; i < studentCount; i++) {
            	//Check that student name cannot be empty
                Object nameValue = model.getValueAt(i, 0);
                if (nameValue == null || nameValue.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Student name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    validInput = false;
                    break;
                }
                String name = nameValue.toString().trim();
                
                //Check that score cannot be empty
                List<Double> scores = new ArrayList<>();
                for (int j = 1; j < columnNames.length; j++) {
                    Object scoreValue = model.getValueAt(i, j);
                    if (scoreValue == null || scoreValue.toString().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Score for " + columnNames[j] + " cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                        validInput = false;
                        break;
                    }
                    try {
                    	//Check that score must between 0 -10
                        double score = Double.parseDouble(scoreValue.toString().trim());
                        if (score < 0 || score > 10) throw new NumberFormatException();
                        scores.add(score);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid score for " + columnNames[j] + ". Must be a number between 0 and 10.", "Error", JOptionPane.ERROR_MESSAGE);
                        validInput = false;
                        break;
                    }
                }
                if (!validInput) break;
                students.add(new Student(name, scores));
            }
            
            if (validInput) {
                displayResults(students, columnNames);
                return students;
                
            }
        }
                
    }

    public static void displayResults(List<Student> students, String[] subjects) {
    	//find the highest average score
        double highestAvgScore = students.stream().mapToDouble(s -> s.averageScore).max().orElse(0.0);
        //find all students with highest average score and store into a list
        List<String> topAvgStudents = new ArrayList<>();
        for (Student student : students) {
            if (student.averageScore == highestAvgScore) {
                topAvgStudents.add(student.name);
            }
        }
        
        Map<String, List<String>> topSubjectStudents = new HashMap<>();
        for (int j = 0; j < subjects.length - 1; j++) {
            int subjectIndex = j;
            //find the highest score for each subject
            double highestSubjectScore = students.stream().mapToDouble(s -> s.scores.get(subjectIndex)).max().orElse(0.0);
            //find all students with highest score for each subject
            List<String> topStudents = new ArrayList<>();
            for (Student student : students) {
                if (student.scores.get(subjectIndex) == highestSubjectScore) {
                    topStudents.add(student.name);
                }
            }
            //store all students with highest score into a list
            topSubjectStudents.put(subjects[j + 1], topStudents);
        }
        //Display the list of student with score for each subject and average score
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Name");
        for (int i = 1; i < subjects.length; i++) {
            model.addColumn(subjects[i]);
        }
        model.addColumn("Average Score");
        
        for (Student student : students) {
            Object[] row = new Object[subjects.length + 1];
            row[0] = student.name;
            for (int j = 1; j < subjects.length; j++) {
                row[j] = student.scores.get(j - 1);
            }
            row[subjects.length] = student.averageScore;
            model.addRow(row);
        }
        JTable studentTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        
        //Display the list of students with highest average score and highest score in each subject
        StringBuilder resultText = new StringBuilder("Top Average Score: " + String.join(", ", topAvgStudents) + " (" + highestAvgScore + ")\n\n");
        for (Map.Entry<String, List<String>> entry : topSubjectStudents.entrySet()) {
            resultText.append("Top " + entry.getKey() + " Score: " + String.join(", ", entry.getValue()) + "\n");
        }
        
        JTextArea textArea = new JTextArea(resultText.toString());
        textArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(textArea);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(resultScrollPane, BorderLayout.SOUTH);
        
        JOptionPane.showMessageDialog(null, panel, "Results", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void saveToJson(List<Student> students, String fileName) {
    	//Store data to file  to re-use if need
        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(students, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        List<Student> students = getStudentData();
        saveToJson(students, "students.json");
    }
}
