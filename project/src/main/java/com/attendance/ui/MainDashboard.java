package com.attendance.ui;

import com.attendance.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Main dashboard window after successful login
 */
public class MainDashboard extends JFrame {
    private final User currentUser;
    private JTabbedPane tabbedPane;

    public MainDashboard(User user) {
        this.currentUser = user;
        
        setTitle("Barcode Attendance System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        initializeUI();
    }

    private void initializeUI() {
        // Set up the main layout
        setLayout(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabbed pane for different sections
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Add the scanner panel (always available)
        ScannerPanel scannerPanel = new ScannerPanel(currentUser);
        tabbedPane.addTab("Scanner", new ImageIcon(), scannerPanel, "Scan student barcodes");

        // Add attendance panel
        AttendancePanel attendancePanel = new AttendancePanel(currentUser);
        tabbedPane.addTab("Attendance Records", new ImageIcon(), attendancePanel, "View and manage attendance records");

        // Add student management panel (admin only)
        if (currentUser.isAdmin()) {
            StudentPanel studentPanel = new StudentPanel();
            tabbedPane.addTab("Student Management", new ImageIcon(), studentPanel, "Manage student information");
            
            // Add reports panel (admin only)
            ReportsPanel reportsPanel = new ReportsPanel();
            tabbedPane.addTab("Reports", new ImageIcon(), reportsPanel, "Generate attendance reports");
        }

        // Add status bar at the bottom
        JPanel statusBar = createStatusBar();
        
        // Add panels to the frame
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        
        // Add window listener to handle closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Perform cleanup before closing
                scannerPanel.stopCamera();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(30, 136, 229)); // Primary blue
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // App title
        JLabel titleLabel = new JLabel("Barcode Attendance System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        // User info and logout panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userLabel.setForeground(Color.WHITE);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(this::handleLogout);
        
        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(logoutButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel timeLabel = new JLabel(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Update time every second
        new Timer(1000, e -> {
            timeLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        }).start();
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(timeLabel, BorderLayout.EAST);
        
        return statusBar;
    }

    private void handleLogout(ActionEvent e) {
        // Stop the camera if it's running
        ((ScannerPanel) tabbedPane.getComponentAt(0)).stopCamera();
        
        // Show login screen
        EventQueue.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose(); // Close this window
        });
    }
}