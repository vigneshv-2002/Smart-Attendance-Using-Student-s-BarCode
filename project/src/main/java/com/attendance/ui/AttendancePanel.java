package com.attendance.ui;

import com.attendance.DatabaseManager;
import com.attendance.model.Attendance;
import com.attendance.model.User;
import com.attendance.util.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Panel for viewing and managing attendance records
 */
public class AttendancePanel extends JPanel {
    private final User currentUser;
    private final JTable attendanceTable;
    private final DefaultTableModel tableModel;
    private final JSpinner dateSpinner;
    private final JButton refreshButton;
    private final JButton exportButton;
    private final JLabel recordCountLabel;

    public AttendancePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model with non-editable cells
        tableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"ID", "Student ID", "Name", "Scan Time", "Status"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        // Create the attendance table
        attendanceTable = new JTable(tableModel);
        attendanceTable.getTableHeader().setReorderingAllowed(false);
        attendanceTable.getTableHeader().setResizingAllowed(true);
        
        // Set column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        // Center align certain columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        attendanceTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        attendanceTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        attendanceTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        // Add table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // North panel for controls
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Date selector
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Select Date:"));
        
        // Create date spinner with today's date
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateModel.setValue(new Date());
        dateSpinner = new JSpinner(dateModel);
        
        // Customize date format
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        datePanel.add(dateSpinner);
        
        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this::refreshAttendanceData);
        datePanel.add(refreshButton);
        
        // Export button
        exportButton = new JButton("Export to Excel");
        exportButton.addActionListener(this::exportToExcel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(exportButton);
        
        controlPanel.add(datePanel, BorderLayout.WEST);
        controlPanel.add(buttonPanel, BorderLayout.EAST);
        
        // South panel for status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recordCountLabel = new JLabel("0 records found");
        statusPanel.add(recordCountLabel);
        
        // Add components to panel
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Load attendance data for today
        loadAttendanceData(new Date());
    }

    private void refreshAttendanceData(ActionEvent event) {
        Date selectedDate = (Date) dateSpinner.getValue();
        loadAttendanceData(selectedDate);
    }

    private void loadAttendanceData(Date date) {
        // Clear existing data
        tableModel.setRowCount(0);
        
        try {
            // Get attendance data for the selected date
            List<Attendance> attendanceList = DatabaseManager.getInstance().getAttendanceByDate(date);
            
            // Format for displaying time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            
            // Add data to table
            for (Attendance attendance : attendanceList) {
                tableModel.addRow(new Object[] {
                    attendance.getId(),
                    attendance.getStudentId(),
                    attendance.getStudent().getFullName(),
                    timeFormat.format(attendance.getScanTime()),
                    attendance.getStatus()
                });
            }
            
            // Update record count
            recordCountLabel.setText(attendanceList.size() + " records found");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading attendance data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void exportToExcel(ActionEvent event) {
        try {
            Date selectedDate = (Date) dateSpinner.getValue();
            
            // Format the date for the filename
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format(selectedDate);
            
            // Create file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Attendance Report");
            fileChooser.setSelectedFile(new File("Attendance_" + formattedDate + ".xlsx"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                
                // Ensure file has .xlsx extension
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.endsWith(".xlsx")) {
                    filePath += ".xlsx";
                    fileToSave = new File(filePath);
                }
                
                // Get attendance data
                List<Attendance> attendanceList = DatabaseManager.getInstance().getAttendanceByDate(selectedDate);
                
                // Export to Excel
                ExcelExporter.exportAttendance(attendanceList, fileToSave, selectedDate);
                
                JOptionPane.showMessageDialog(this,
                    "Attendance data exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error exporting to Excel: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}