import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import com.mongodb.MongoWriteException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HostelManagementSystem {

    private static final String CONNECTION_STRING = "CONNECTION_STRING";
    private static final String DATABASE_NAME = "hostel";
    private static final String COLLECTION_NAME = "students";
   

    private static MongoCollection<Document> getCollection() {
        return MongoClients.create(CONNECTION_STRING).getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
    }

    private static void createUniqueIndex() {
        MongoCollection<Document> collection = getCollection();
        collection.createIndex(new Document("Student_ID", 1), new IndexOptions().unique(true));
    }
    
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    
    private static class LoginPage extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginPage() {
            setTitle("Login");
            setSize(300, 150);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            usernameField = new JTextField(15);
            panel.add(usernameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            passwordField = new JPasswordField(15);
            panel.add(passwordField, gbc);

            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(this::performLogin);
            gbc.gridx = 1;
            gbc.gridy = 2;
            panel.add(loginButton, gbc);

            add(panel);
        }

        private void performLogin(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                dispose();
                SwingUtilities.invokeLater(() -> new StudentTable().setVisible(true));
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private abstract static class StudentFormBase extends JPanel {
        protected JTextField studentIdField, nameField, contactInfoField;
        protected JComboBox<String> genderComboBox, departmentComboBox, yearComboBox, semesterComboBox;
        protected JButton actionButton;
        protected StudentTable studentTable;

        public StudentFormBase(String buttonLabel, StudentTable studentTable) {
            this.studentTable = studentTable;
            initializeComponents(buttonLabel);
            arrangeComponents();
            actionButton.addActionListener(this::onActionButtonClick);
        }

        private void initializeComponents(String buttonLabel) {
            studentIdField = new JTextField(20);
            nameField = new JTextField(20);
            contactInfoField = new JTextField(20);
           

            genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Others"});
            departmentComboBox = new JComboBox<>(new String[]{
                "Computer Science and Engineering", 
                "Information Science and Engineering", 
                "Artificial Intelligence and Machine Learning", 
                "Electronics and Communication Engineering", 
                "Electronics and Instrumentation Engineering", 
                "Mechanical Engineering", 
                "Civil Engineering"
            });

            yearComboBox = new JComboBox<>(new String[]{"1st", "2nd", "3rd", "4th"});
            semesterComboBox = new JComboBox<>(new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
            
            actionButton = new JButton(buttonLabel);
        }

        private void arrangeComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            String[] labels = {"Student ID:", "Name:", "Contact Info:", "Gender:", "Department:", "Year of Study:", "Semester:"};
            Component[] fields = {studentIdField, nameField, contactInfoField, genderComboBox, departmentComboBox, yearComboBox, semesterComboBox};

            for (int i = 0; i < labels.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i;
                add(new JLabel(labels[i]), gbc);
                gbc.gridx = 1;
                add(fields[i], gbc);
            }

            gbc.gridx = 0;
            gbc.gridy = labels.length;
            gbc.gridwidth = 2;
            add(actionButton, gbc);
        }

        protected void clearFields() {
            studentIdField.setText("");
            nameField.setText("");
            contactInfoField.setText("");
            genderComboBox.setSelectedIndex(0);
            departmentComboBox.setSelectedIndex(0);
            yearComboBox.setSelectedIndex(0);
            semesterComboBox.setSelectedIndex(0);
        }

        protected abstract void onActionButtonClick(ActionEvent e);
    }

    public static class StudentForm extends StudentFormBase {
        public StudentForm(StudentTable studentTable) {
            super("Save", studentTable);
        }

        @Override
        protected void onActionButtonClick(ActionEvent e) {
            try {
                int studentId = Integer.parseInt(studentIdField.getText());
                MongoCollection<Document> collection = getCollection();

                
                Document existingStudent = collection.find(Filters.eq("Student_ID", studentId)).first();
                if (existingStudent != null) {
                    JOptionPane.showMessageDialog(this, "Student record ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Document doc = new Document("Student_ID", studentId)
                        .append("Name", nameField.getText())
                        .append("Contact_Info", contactInfoField.getText())
                        .append("Gender", genderComboBox.getSelectedItem().toString())
                        .append("Department", departmentComboBox.getSelectedItem().toString())
                        .append("Year_of_Study", yearComboBox.getSelectedItem().toString())
                        .append("Semester", semesterComboBox.getSelectedItem().toString());
                collection.insertOne(doc);
                JOptionPane.showMessageDialog(this, "Student record saved successfully!");
                clearFields();
                studentTable.refreshTableData();
            } catch (MongoWriteException ex) {
                JOptionPane.showMessageDialog(this, "Student record ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Student record ID format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class StudentUpdateForm extends StudentFormBase {
        public StudentUpdateForm(StudentTable studentTable) {
            super("Update", studentTable);
        }

        @Override
        protected void onActionButtonClick(ActionEvent e) {
            try {
                int studentId = Integer.parseInt(studentIdField.getText());
                Document update = new Document("$set", new Document("Name", nameField.getText())
                        .append("Contact_Info", contactInfoField.getText())
                        .append("Gender", genderComboBox.getSelectedItem().toString())
                        .append("Department", departmentComboBox.getSelectedItem().toString())
                        .append("Year_of_Study", yearComboBox.getSelectedItem().toString())
                        .append("Semester", semesterComboBox.getSelectedItem().toString()));

                UpdateResult result = getCollection().updateOne(Filters.eq("Student_ID", studentId), update);
                JOptionPane.showMessageDialog(this, result.getMatchedCount() > 0 ? "Student record updated successfully!" : "Student record not found!",
                        result.getMatchedCount() > 0 ? "Success" : "Error", JOptionPane.INFORMATION_MESSAGE);
                if (result.getMatchedCount() > 0) {
                    clearFields();
                    studentTable.refreshTableData();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Student ID format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class StudentTable extends JFrame {
        final JTable table;
        private JTextField searchField;
        private JButton searchButton, showAllButton;
        private CardLayout cardLayout;
        private JPanel cardPanel;

        public StudentTable() {
            setTitle("Student Record List");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            table = new JTable();
            loadStudentData();

            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);
            cardPanel.add(createTablePanel(), "Table");
            cardPanel.add(createFormPanel(new StudentForm(this)), "Add");
            cardPanel.add(createFormPanel(new StudentUpdateForm(this)), "Update");

            JPanel sidebar = new JPanel(new GridLayout(0, 1));
            sidebar.add(createSidebarButton("View Records", e -> cardLayout.show(cardPanel, "Table")));
            sidebar.add(createSidebarButton("Add Record", e -> cardLayout.show(cardPanel, "Add")));
            sidebar.add(createSidebarButton("Update Record", e -> cardLayout.show(cardPanel, "Update")));
            sidebar.add(createSidebarButton("Delete Record", e -> deleteStudent()));

            JPanel searchPanel = new JPanel();
            searchPanel.add(new JLabel("Search by Student ID:"));
            searchField = new JTextField(15);
            searchPanel.add(searchField);
            searchButton = new JButton("Search");
            searchButton.addActionListener(e -> searchStudent());
            searchPanel.add(searchButton);
            showAllButton = new JButton("Show All");
            showAllButton.addActionListener(e -> showAllStudents());
            searchPanel.add(showAllButton);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(searchPanel, BorderLayout.SOUTH);
            mainPanel.add(cardPanel, BorderLayout.CENTER);
            
            setLayout(new BorderLayout());
            add(sidebar, BorderLayout.WEST);
            add(mainPanel, BorderLayout.CENTER);
        }

        private JPanel createTablePanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            return panel;
        }

        private JPanel createFormPanel(JPanel form) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(form, BorderLayout.CENTER);
            return panel;
        }

        private JButton createSidebarButton(String text, ActionListener action) {
            JButton button = new JButton(text);
            button.addActionListener(action);
            return button;
        }

        private void loadStudentData() {
            DefaultTableModel model = new DefaultTableModel(new String[]{"Student ID", "Name", "Contact Info", "Gender", "Department", "Year of Study", "Semester"}, 0);
            for (Document doc : getCollection().find()) {
                model.addRow(new Object[]{
                        doc.getInteger("Student_ID"),
                        doc.getString("Name"),
                        doc.getString("Contact_Info"),
                        doc.getString("Gender"),
                        doc.getString("Department"),
                        doc.getString("Year_of_Study"),
                        doc.getString("Semester")
                });
            }
            table.setModel(model);
            table.setRowHeight(30);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setSelectionBackground(new Color(0xB3E5FC));
            table.setSelectionForeground(Color.BLACK);
            table.setCellSelectionEnabled(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }

        public void refreshTableData() {
            loadStudentData();
        }

        private void searchStudent() {
            String studentIdStr = searchField.getText();
            if (studentIdStr != null && !studentIdStr.isEmpty()) {
                try {
                    int studentId = Integer.parseInt(studentIdStr);
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    model.setRowCount(0); 
                    Document doc = getCollection().find(Filters.eq("Student_ID", studentId)).first();
                    if (doc != null) {
                        model.addRow(new Object[]{
                                doc.getInteger("Student_ID"),
                                doc.getString("Name"),
                                doc.getString("Contact_Info"),
                                doc.getString("Gender"),
                                doc.getString("Department"),
                                doc.getString("Year_of_Study"),
                                doc.getString("Semester")
                        });
                    } else {
                        JOptionPane.showMessageDialog(this, "Student record not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Student record ID format!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void showAllStudents() {
            loadStudentData();
        }

        private void deleteStudent() {
            String studentIdStr = JOptionPane.showInputDialog(this, "Enter Student record ID to delete:");
            if (studentIdStr != null && !studentIdStr.isEmpty()) {
                try {
                    int studentId = Integer.parseInt(studentIdStr);
                    DeleteResult result = getCollection().deleteOne(Filters.eq("Student_ID", studentId));
                    JOptionPane.showMessageDialog(this, result.getDeletedCount() > 0 ? "Student record deleted successfully!" : "Student record not found!",
                            result.getDeletedCount() > 0 ? "Success" : "Error", JOptionPane.INFORMATION_MESSAGE);
                    if (result.getDeletedCount() > 0) {
                        refreshTableData();
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Student record ID format!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true)); 
    }
}