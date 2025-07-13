package com.attendance.ui;

import com.attendance.DatabaseManager;
import com.attendance.model.Attendance;
import com.attendance.util.EmailSender;
import com.attendance.util.ExcelExporter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for generating attendance reports and statistics
 */
public class ReportsPanel extends JPanel {
    private final JSpinner startDateSpinner;
    private final JSpinner endDateSpinner;
    private final JComboBox<String> reportTypeComboBox;
    private final JButton generateButton;
    private final JButton emailButton;
    private final JTextField emailTextField;
    private final JPanel chartPanel;
    private File lastGeneratedReport = null;

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control panel for report options
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Report Options"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Date selectors
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("Start Date:"), gbc);
        
        // Set up start date (default to 7 days ago)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date startDate = calendar.getTime();
        
        SpinnerDateModel startDateModel = new SpinnerDateModel(startDate, null, null, Calendar.DAY_OF_MONTH);
        startDateSpinner = new JSpinner(startDateModel);
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        
        gbc.gridx = 1;
        controlPanel.add(startDateSpinner, gbc);
        
        gbc.gridx = 2;
        controlPanel.add(new JLabel("End Date:"), gbc);
        
        // Set up end date (default to today)
        SpinnerDateModel endDateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        endDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        
        gbc.gridx = 3;
        controlPanel.add(endDateSpinner, gbc);
        
        // Report type selector
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Report Type:"), gbc);
        
        reportTypeComboBox = new JComboBox<>(new String[] {
            "Daily Attendance Summary",
            "Student Attendance Details",
            "Attendance Trends"
        });
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        controlPanel.add(reportTypeComboBox, gbc);
        
        // Generate button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        
        generateButton = new JButton("Generate Report");
        generateButton.addActionListener(this::generateReport);
        controlPanel.add(generateButton, gbc);
        
        // Email section
        gbc.gridx = 1;
        controlPanel.add(new JLabel("Email To:"), gbc);
        
        gbc.gridx = 2;
        emailTextField = new JTextField(20);
        emailTextField.putClientProperty("JTextField.placeholderText", "Enter recipient email");
        controlPanel.add(emailTextField, gbc);
        
        gbc.gridx = 3;
        emailButton = new JButton("Send Email");
        emailButton.addActionListener(this::sendReportEmail);
        emailButton.setEnabled(false); // Initially disabled until report is generated
        controlPanel.add(emailButton, gbc);
        
        // Create chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Add a placeholder label
        JLabel placeholderLabel = new JLabel("Generate a report to view statistics", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        placeholderLabel.setForeground(Color.GRAY);
        chartPanel.add(placeholderLabel, BorderLayout.CENTER);
        
        // Add panels to main panel
        add(controlPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    private void generateReport(ActionEvent e) {
        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this,
                "Start date must be before or equal to end date",
                "Date Range Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Format dates for filename
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String filename = reportType.replace(" ", "_") + "_" + 
                             dateFormat.format(startDate) + "_to_" + 
                             dateFormat.format(endDate) + ".xlsx";
            
            // Create file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Report");
            fileChooser.setSelectedFile(new File(filename));
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                
                // Ensure file has .xlsx extension
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.endsWith(".xlsx")) {
                    filePath += ".xlsx";
                    fileToSave = new File(filePath);
                }
                
                // Generate report based on type
                if ("Daily Attendance Summary".equals(reportType)) {
                    generateDailyAttendanceSummary(startDate, endDate, fileToSave);
                } else if ("Student Attendance Details".equals(reportType)) {
                    generateStudentAttendanceDetails(startDate, endDate, fileToSave);
                } else if ("Attendance Trends".equals(reportType)) {
                    generateAttendanceTrends(startDate, endDate, fileToSave);
                }
                
                // Store last generated report
                lastGeneratedReport = fileToSave;
                
                // Enable email button
                emailButton.setEnabled(true);
                
                JOptionPane.showMessageDialog(this,
                    "Report generated successfully at:\n" + fileToSave.getAbsolutePath(),
                    "Report Generated",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error generating report: " + ex.getMessage(),
                "Report Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void generateDailyAttendanceSummary(Date startDate, Date endDate, File file) throws Exception {
        // TODO: Implement full report generation
        // For now, we'll generate a sample chart
        
        // Create a sample dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Populate with random data for demo
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        
        Random random = new Random();
        
        while (!calendar.getTime().after(endDate)) {
            String date = dateFormat.format(calendar.getTime());
            int present = 15 + random.nextInt(10);
            int absent = 5 + random.nextInt(5);
            
            dataset.addValue(present, "Present", date);
            dataset.addValue(absent, "Absent", date);
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
            "Daily Attendance Summary",
            "Date",
            "Number of Students",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Update chart panel
        updateChartPanel(chart);
        
        // Generate Excel file
        ExcelExporter.exportDailyAttendanceSummary(dataset, file, startDate, endDate);
    }

    private void generateStudentAttendanceDetails(Date startDate, Date endDate, File file) throws Exception {
        // Create a placeholder chart for now
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Example student data
        String[] students = {"John Smith", "Maria Garcia", "James Johnson", "Emma Wilson"};
        
        // Generate random data for each student
        Random random = new Random();
        for (String student : students) {
            dataset.addValue(70 + random.nextInt(30), "Attendance %", student);
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
            "Student Attendance Percentage",
            "Student",
            "Attendance %",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Update chart panel
        updateChartPanel(chart);
        
        // Generate Excel file (placeholder for now)
        ExcelExporter.exportStudentAttendanceDetails(dataset, file, startDate, endDate);
    }

    private void generateAttendanceTrends(Date startDate, Date endDate, File file) throws Exception {
        // Create a placeholder line chart for attendance trends
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Generate sample data
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        
        Random random = new Random();
        double attendance = 80.0;
        
        while (!calendar.getTime().after(endDate)) {
            String date = dateFormat.format(calendar.getTime());
            
            // Fluctuate the attendance rate slightly
            attendance += (random.nextDouble() - 0.5) * 10;
            if (attendance > 100) attendance = 100;
            if (attendance < 50) attendance = 50;
            
            dataset.addValue(attendance, "Attendance Rate %", date);
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
            "Attendance Rate Trends",
            "Date",
            "Attendance Rate %",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Update chart panel
        updateChartPanel(chart);
        
        // Generate Excel file
        ExcelExporter.exportAttendanceTrends(dataset, file, startDate, endDate);
    }

    private void updateChartPanel(JFreeChart chart) {
        // Clear existing content
        chartPanel.removeAll();
        
        // Add the chart
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(500, 300));
        panel.setMouseWheelEnabled(true);
        
        chartPanel.add(panel, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private void sendReportEmail(ActionEvent e) {
        if (lastGeneratedReport == null) {
            JOptionPane.showMessageDialog(this,
                "Please generate a report first",
                "No Report Available",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String emailTo = emailTextField.getText().trim();
        if (emailTo.isEmpty() || !isValidEmail(emailTo)) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address",
                "Invalid Email",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show email configuration dialog
        EmailConfigDialog dialog = new EmailConfigDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            String smtpHost = dialog.getSmtpHost();
            int smtpPort = dialog.getSmtpPort();
            String username = dialog.getUsername();
            String password = dialog.getPassword();
            
            try {
                // Send email
                EmailSender.sendAttendanceReport(
                    smtpHost,
                    smtpPort,
                    username,
                    password,
                    username, // From email is the same as username
                    emailTo,
                    "Attendance Report",
                    "Please find the attendance report attached.",
                    lastGeneratedReport
                );
                
                JOptionPane.showMessageDialog(this,
                    "Report sent successfully to " + emailTo,
                    "Email Sent",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error sending email: " + ex.getMessage(),
                    "Email Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Dialog for email configuration
     */
    private static class EmailConfigDialog extends JDialog {
        private final JTextField smtpHostField;
        private final JTextField smtpPortField;
        private final JTextField usernameField;
        private final JPasswordField passwordField;
        private boolean confirmed = false;

        public EmailConfigDialog(Window owner) {
            super(owner, "Email Configuration", ModalityType.APPLICATION_MODAL);
            setSize(400, 250);
            setLocationRelativeTo(owner);
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // SMTP Host
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("SMTP Host:"), gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            smtpHostField = new JTextField("smtp.gmail.com", 20);
            panel.add(smtpHostField, gbc);
            
            // SMTP Port
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("SMTP Port:"), gbc);
            
            gbc.gridx = 1;
            smtpPortField = new JTextField("587", 20);
            panel.add(smtpPortField, gbc);
            
            // Username/Email
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("Email:"), gbc);
            
            gbc.gridx = 1;
            usernameField = new JTextField(20);
            panel.add(usernameField, gbc);
            
            // Password
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("Password:"), gbc);
            
            gbc.gridx = 1;
            passwordField = new JPasswordField(20);
            panel.add(passwordField, gbc);
            
            // Buttons
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> {
                if (validateFields()) {
                    confirmed = true;
                    dispose();
                }
            });
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            panel.add(buttonPanel, gbc);
            
            add(panel);
        }

        private boolean validateFields() {
            if (smtpHostField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "SMTP Host is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            try {
                int port = Integer.parseInt(smtpPortField.getText().trim());
                if (port <= 0 || port > 65535) {
                    JOptionPane.showMessageDialog(this, "Invalid port number", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Port must be a number", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (usernameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Password is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            return true;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public String getSmtpHost() {
            return smtpHostField.getText().trim();
        }

        public int getSmtpPort() {
            return Integer.parseInt(smtpPortField.getText().trim());
        }

        public String getUsername() {
            return usernameField.getText().trim();
        }

        public String getPassword() {
            return new String(passwordField.getPassword());
        }
    }
}