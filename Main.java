import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Main {
    private static Connection conn;
    private static JFrame mainFrame;
    private static JPanel contentPanel;

    public static void main(String[] args) {
        connectDatabase();
        createMainFrame();
    }

    // Connect to PostgreSQL Database
  public static void connectDatabase() {
    try {
        // Ensure the driver is loaded
        Class.forName("org.postgresql.Driver");

        // Establish the connection
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/university_management", "username", "password");

    } catch (ClassNotFoundException e) {
        System.out.println("PostgreSQL JDBC driver not found.");
        e.printStackTrace();
    } catch (SQLException e) {
        System.out.println("Connection failed.");
        e.printStackTrace();
    }
}

    

    // Create Main GUI Window with Sidebar
    private static void createMainFrame() {
        mainFrame = new JFrame("University Management with Side Menu");
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create Sidebar
        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new GridLayout(6, 1));  // Updated grid layout for more buttons
        sideMenu.setPreferredSize(new Dimension(200, 400));

        // Create Buttons for the Sidebar
        JButton addClassButton = new JButton("Add Class");
        JButton addStudentButton = new JButton("Add Student to Class");
        JButton viewClassesButton = new JButton("View Classes");
        JButton viewStudentsButton = new JButton("View Students");
        JButton viewAssignmentsButton = new JButton("View Assignments");
        JButton logoutButton = new JButton("Logout");

        // Add Event Listeners
        addClassButton.addActionListener(e -> showAddClassPanel());
        addStudentButton.addActionListener(e -> showAddStudentPanel());
        viewClassesButton.addActionListener(e -> showViewClassesPanel());
        viewStudentsButton.addActionListener(e -> showViewStudentsPanel());
        viewAssignmentsButton.addActionListener(e -> showViewAssignmentsPanel());
        logoutButton.addActionListener(e -> mainFrame.dispose());

        // Add Buttons to Sidebar
        sideMenu.add(addClassButton);
        sideMenu.add(addStudentButton);
        sideMenu.add(viewClassesButton);
        sideMenu.add(viewStudentsButton);
        sideMenu.add(viewAssignmentsButton);
        sideMenu.add(logoutButton);

        // Create a Content Panel (right side)
        contentPanel = new JPanel();
        contentPanel.setLayout(new CardLayout());

        // Add panels to the main frame
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(sideMenu, BorderLayout.WEST);
        mainFrame.add(contentPanel, BorderLayout.CENTER);

        mainFrame.setVisible(true);
    }

    // Show Add Class Panel
    private static void showAddClassPanel() {
        JPanel addClassPanel = new JPanel();
        JTextField classNameField = new JTextField(20);
        JButton addButton = new JButton("Add Class");

        addButton.addActionListener(e -> {
            String className = classNameField.getText();
            addClassToDatabase(className);
        });

        addClassPanel.add(new JLabel("Class Name:"));
        addClassPanel.add(classNameField);
        addClassPanel.add(addButton);

        // Update content panel
        contentPanel.removeAll();
        contentPanel.add(addClassPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Add Class to Database
    private static void addClassToDatabase(String className) {
        try {
            PreparedStatement pst = conn.prepareStatement("INSERT INTO classes (class_name) VALUES (?)");
            pst.setString(1, className);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(mainFrame, "Class added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show Add Student to Class Panel
    private static void showAddStudentPanel() {
        JPanel addStudentPanel = new JPanel(new GridLayout(4, 2));
        JTextField studentNameField = new JTextField(20);
        JTextField studentEmailField = new JTextField(20);
        JComboBox<String> classComboBox = new JComboBox<>();

        // Load classes from database into combo box
        loadClassesIntoComboBox(classComboBox);

        JButton addButton = new JButton("Add Student");

        addButton.addActionListener(e -> {
            String studentName = studentNameField.getText();
            String studentEmail = studentEmailField.getText();
            String selectedClass = (String) classComboBox.getSelectedItem();
            addStudentToClass(studentName, studentEmail, selectedClass);
        });

        addStudentPanel.add(new JLabel("Student Name:"));
        addStudentPanel.add(studentNameField);
        addStudentPanel.add(new JLabel("Student Email:"));
        addStudentPanel.add(studentEmailField);
        addStudentPanel.add(new JLabel("Class:"));
        addStudentPanel.add(classComboBox);
        addStudentPanel.add(addButton);

        // Update content panel
        contentPanel.removeAll();
        contentPanel.add(addStudentPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Load classes into JComboBox
    private static void loadClassesIntoComboBox(JComboBox<String> classComboBox) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT class_name FROM classes");

            while (rs.next()) {
                classComboBox.addItem(rs.getString("class_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add Student to Class in Database
    private static void addStudentToClass(String name, String email, String className) {
        try {
            // Get class ID based on class name
            PreparedStatement pstClass = conn.prepareStatement("SELECT class_id FROM classes WHERE class_name = ?");
            pstClass.setString(1, className);
            ResultSet rs = pstClass.executeQuery();

            if (rs.next()) {
                int classId = rs.getInt("class_id");

                // Insert student
                PreparedStatement pstStudent = conn.prepareStatement("INSERT INTO students (name, email) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                pstStudent.setString(1, name);
                pstStudent.setString(2, email);
                pstStudent.executeUpdate();

                ResultSet rsStudent = pstStudent.getGeneratedKeys();
                if (rsStudent.next()) {
                    int studentId = rsStudent.getInt(1);

                    // Insert into student_classes
                    PreparedStatement pstAssign = conn.prepareStatement("INSERT INTO student_classes (student_id, class_id) VALUES (?, ?)");
                    pstAssign.setInt(1, studentId);
                    pstAssign.setInt(2, classId);
                    pstAssign.executeUpdate();

                    JOptionPane.showMessageDialog(mainFrame, "Student added to class successfully!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show View Classes Panel
    private static void showViewClassesPanel() {
        JPanel viewClassesPanel = new JPanel();
        JTextArea classesArea = new JTextArea(10, 40);
        classesArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(classesArea);

        // Load classes from database
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM classes");

            StringBuilder classesText = new StringBuilder();
            while (rs.next()) {
                classesText.append("Class ID: ").append(rs.getInt("class_id"))
                        .append(", Class Name: ").append(rs.getString("class_name")).append("\n");
            }

            classesArea.setText(classesText.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        viewClassesPanel.add(scrollPane);

        // Update content panel
        contentPanel.removeAll();
        contentPanel.add(viewClassesPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Show View Students Panel
    private static void showViewStudentsPanel() {
        JPanel viewStudentsPanel = new JPanel();
        JTextArea studentsArea = new JTextArea(10, 40);
        studentsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(studentsArea);

        // Load students from database
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");

            StringBuilder studentsText = new StringBuilder();
            while (rs.next()) {
                studentsText.append("Student ID: ").append(rs.getInt("student_id"))
                        .append(", Name: ").append(rs.getString("name"))
                        .append(", Email: ").append(rs.getString("email")).append("\n");
            }

            studentsArea.setText(studentsText.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        viewStudentsPanel.add(scrollPane);

        // Update content panel
        contentPanel.removeAll();
        contentPanel.add(viewStudentsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Show View Assignments Panel
    private static void showViewAssignmentsPanel() {
        JPanel viewAssignmentsPanel = new JPanel();
        JTextArea assignmentsArea = new JTextArea(10, 40);
        assignmentsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(assignmentsArea);

        // Load student-class assignments
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT s.name, c.class_name FROM students s " + "JOIN student_classes sc ON s.student_id = sc.student_id "+ "JOIN classes c ON sc.class_id = c.class_id");

            StringBuilder assignmentsText = new StringBuilder();
            while (rs.next()) {
                assignmentsText.append("Student: ").append(rs.getString("name")).append(", Class: ").append(rs.getString("class_name")).append("\n");
            }

            assignmentsArea.setText(assignmentsText.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        viewAssignmentsPanel.add(scrollPane);

        // Update content panel
        contentPanel.removeAll();
        contentPanel.add(viewAssignmentsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}