package com.attendance.ui;

import com.attendance.DatabaseManager;
import com.attendance.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

/**
 * Login window for user authentication
 */
public class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JLabel statusLabel;

    public LoginFrame() {
        setTitle("Barcode Attendance System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create the main panel with a beautiful gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(30, 136, 229);
                Color color2 = new Color(33, 150, 243);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Header panel with app title
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel titleLabel = new JLabel("Barcode Attendance System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form panel with login fields
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Username label and field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameField = new JTextField(20);
        usernameField.putClientProperty("JComponent.roundRect", true);
        usernameField.putClientProperty("JTextField.placeholderText", "Enter your username");

        // Password label and field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordField = new JPasswordField(20);
        passwordField.putClientProperty("JComponent.roundRect", true);
        passwordField.putClientProperty("JTextField.placeholderText", "Enter your password");

        // Status label for login errors
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(255, 87, 34)); // Error orange color
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(67, 160, 71)); // Green color
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add login handler
        loginButton.addActionListener(this::handleLogin);

        // Add components to form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(statusLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(15, 10, 5, 10);
        formPanel.add(loginButton, gbc);

        // Add keyboard action for enter key
        getRootPane().setDefaultButton(loginButton);

        // Create a wrapper panel to center the form
        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setOpaque(false);
        wrapperPanel.setLayout(new BorderLayout());
        wrapperPanel.add(Box.createVerticalStrut(30), BorderLayout.NORTH);
        wrapperPanel.add(formPanel, BorderLayout.CENTER);

        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(wrapperPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return;
        }

        try {
            // Attempt to authenticate
            User user = DatabaseManager.getInstance().authenticateUser(username, password);
            
            if (user != null) {
                // Successful login
                statusLabel.setText("");
                
                // Open the main dashboard
                EventQueue.invokeLater(() -> {
                    MainDashboard dashboard = new MainDashboard(user);
                    dashboard.setVisible(true);
                    dispose(); // Close the login window
                });
            } else {
                // Failed login
                statusLabel.setText("Invalid username or password");
                passwordField.setText("");
            }
        } catch (SQLException ex) {
            statusLabel.setText("Login error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}