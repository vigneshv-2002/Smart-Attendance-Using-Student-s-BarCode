    package com.attendance;

import com.attendance.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the Barcode Attendance System
 */
public class Main {
    public static void main(String[] args) {
        // Set up the modern look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Set custom colors
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Button.background", new Color(30, 136, 229)); // Primary blue
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.hoverBackground", new Color(25, 118, 210));
            UIManager.put("Button.pressedBackground", new Color(21, 101, 192));
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Initialize the database
        try {
            DatabaseManager.getInstance().initDatabase();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Failed to initialize database: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Launch the application UI
        EventQueue.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}