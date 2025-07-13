package com.attendance.ui;

import com.attendance.DatabaseManager;
import com.attendance.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for managing student information
 */
public class StudentPanel extends JPanel {
    private final JTable studentTable;
    private final DefaultTableModel tableModel;
    private final JTextField searchField;
    private final JButton addButton;
    private final JButton refreshButton;

    // Fields for adding a new student
    private final JTextField idField = new JTextField(15);
    private final JTextField barcodeField = new JTextField(15);
    private final JTextField firstNameField = new JTextField(15);
    private final JTextField lastNameField = new JTextField(15);
    private final JTextField emailField = new JTextField(15);

    public StudentPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model
        tableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"ID", "Student ID", "Barcode", "First Name", "Last Name", "Email"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        // Create student table
        studentTable = new JTable(tableModel);
        studentTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        
        // Center align ID column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        studentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(studentTable);
        
        // Create control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Enter name or ID");
        searchPanel.add(searchField);
        
        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(this::searchStudents);
        searchPanel.add(searchButton);
        
        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStudents());
        searchPanel.add(refreshButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Add button
        addButton = new JButton("Add Student");
        addButton.addActionListener(this::showAddStudentDialog);
        buttonPanel.add(addButton);
        
        // Add button to edit selected student
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(this::editSelectedStudent);
        buttonPanel.add(editButton);
        
        controlPanel.add(searchPanel, BorderLayout.WEST);
        controlPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Add components to panel
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load students
        loadStudents();
    }

    private void loadStudents() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        try {
            // Get all students
            List<Student> students = DatabaseManager.getInstance().getAllStudents();
            
            // Add to table
            for (Student student : students) {
                tableModel.addRow(new Object[] {
                    student.getId(),
                    student.getStudentId(),
                    student.getBarcode(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getEmail()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading students: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void searchStudents(ActionEvent e) {
        String searchText = searchField.getText().trim().toLowerCase();
        
        if (searchText.isEmpty()) {
            loadStudents();
            return;
        }
        
        // Filter table based on search text
        tableModel.setRowCount(0);
        
        try {
            List<Student> students = DatabaseManager.getInstance().getAllStudents();
            
            for (Student student : students) {
                if (student.getStudentId().toLowerCase().contains(searchText) ||
                    student.getFirstName().toLowerCase().contains(searchText) ||
                    student.getLastName().toLowerCase().contains(searchText) ||
                    student.getBarcode().toLowerCase().contains(searchText)) {
                    
                    tableModel.addRow(new Object[] {
                        student.getId(),
                        student.getStudentId(),
                        student.getBarcode(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getEmail()
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error searching students: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showAddStudentDialog(ActionEvent e) {
        // Create panel for student form
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Reset fields
        idField.setText("");
        barcodeField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        
        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Student ID:"), gbc);
        
        gbc.gridx = 1;
        panel.add(idField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Barcode:"), gbc);
        
        gbc.gridx = 1;
        panel.add(barcodeField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        // Show dialog
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            addStudent();
        }
    }

    private void addStudent() {
        // Validate input
        String studentId = idField.getText().trim();
        String barcode = barcodeField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (studentId.isEmpty() || barcode.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Student ID, Barcode, First Name, and Last Name are required",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Create student object
            Student student = new Student(studentId, barcode, firstName, lastName, email);
            
            // Add to database
            boolean success = DatabaseManager.getInstance().addStudent(student);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Student added successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh table
                loadStudents();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to add student",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error adding student: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void editSelectedStudent(ActionEvent e) {
        int selectedRow = studentTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a student to edit",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Get student data from the selected row
        String studentId = tableModel.getValueAt(selectedRow, 1).toString();
        String barcode = tableModel.getValueAt(selectedRow, 2).toString();
        String firstName = tableModel.getValueAt(selectedRow, 3).toString();
        String lastName = tableModel.getValueAt(selectedRow, 4).toString();
        String email = tableModel.getValueAt(selectedRow, 5).toString();
        
        // Populate fields
        idField.setText(studentId);
        barcodeField.setText(barcode);
        firstNameField.setText(firstName);
        lastNameField.setText(lastName);
        emailField.setText(email);
        
        // Create panel for student form
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Student ID:"), gbc);
        
        gbc.gridx = 1;
        panel.add(idField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Barcode:"), gbc);
        
        gbc.gridx = 1;
        panel.add(barcodeField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("First Name:"), gbc);
        
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Last Name:"), gbc);
        
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        panel.add(emailField, gbc);
        
        // Show dialog
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            // TODO: Update student in database
            JOptionPane.showMessageDialog(this,
                "Student information updated",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh table
            loadStudents();
        }
    }
}