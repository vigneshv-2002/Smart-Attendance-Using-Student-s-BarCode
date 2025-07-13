package com.attendance.ui;

import com.attendance.DatabaseManager;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Panel for capturing webcam input and scanning barcodes
 */
public class ScannerPanel extends JPanel {
    private final User currentUser;
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ScheduledExecutorService executor;
    private JLabel scanResultLabel;
    private JLabel studentInfoLabel;
    private JLabel scanStatusLabel;
    private JComboBox<Webcam> webcamSelector;
    private boolean isScanning = false;

    // To avoid duplicate scans
    private String lastScannedBarcode = "";
    private long lastScanTime = 0;
    private final long SCAN_COOLDOWN_MS = 3000; // 3 seconds cooldown between scans

    public ScannerPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeUI();
    }

    private void initializeUI() {
        // North panel for controls
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Webcam selector
        JPanel webcamSelectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel webcamLabel = new JLabel("Select Webcam:");
        webcamSelector = new JComboBox<>();
        
        // Populate webcam selector
        for (Webcam cam : Webcam.getWebcams()) {
            webcamSelector.addItem(cam);
        }
        
        webcamSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Webcam) {
                    setText(((Webcam) value).getName());
                }
                return this;
            }
        });
        
        webcamSelector.addActionListener(e -> switchWebcam((Webcam) webcamSelector.getSelectedItem()));
        
        JButton startButton = new JButton("Start Scanner");
        startButton.addActionListener(e -> {
            if (!isScanning) {
                startScanner();
                startButton.setText("Stop Scanner");
            } else {
                stopScanner();
                startButton.setText("Start Scanner");
            }
        });
        
        webcamSelectorPanel.add(webcamLabel);
        webcamSelectorPanel.add(webcamSelector);
        webcamSelectorPanel.add(startButton);
        
        controlPanel.add(webcamSelectorPanel, BorderLayout.WEST);
        
        // Center panel for webcam view
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Initialize with first webcam
        if (webcamSelector.getItemCount() > 0) {
            webcam = (Webcam) webcamSelector.getSelectedItem();
            if (webcam != null) {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcamPanel = new WebcamPanel(webcam, false);
                webcamPanel.setFPSDisplayed(true);
                webcamPanel.setMirrored(false);
                centerPanel.add(webcamPanel, BorderLayout.CENTER);
            } else {
                JLabel noWebcamLabel = new JLabel("No webcam detected", SwingConstants.CENTER);
                noWebcamLabel.setFont(new Font("Arial", Font.BOLD, 18));
                centerPanel.add(noWebcamLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel noWebcamLabel = new JLabel("No webcam detected", SwingConstants.CENTER);
            noWebcamLabel.setFont(new Font("Arial", Font.BOLD, 18));
            centerPanel.add(noWebcamLabel, BorderLayout.CENTER);
        }
        
        // South panel for scan results
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        scanResultLabel = new JLabel("Waiting for barcode scan...");
        scanResultLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scanResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        studentInfoLabel = new JLabel(" ");
        studentInfoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        studentInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        scanStatusLabel = new JLabel(" ");
        scanStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scanStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        resultPanel.add(scanResultLabel);
        resultPanel.add(Box.createVerticalStrut(5));
        resultPanel.add(studentInfoLabel);
        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(scanStatusLabel);
        
        // Add all panels to main layout
        add(controlPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);
    }

    private void switchWebcam(Webcam newWebcam) {
        // Stop current scanner if running
        if (isScanning) {
            stopScanner();
        }
        
        // Remove current webcam panel
        Component centerComponent = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent instanceof JPanel) {
            JPanel centerPanel = (JPanel) centerComponent;
            centerPanel.removeAll();
            
            // Set up new webcam
            if (newWebcam != null) {
                webcam = newWebcam;
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcamPanel = new WebcamPanel(webcam, false);
                webcamPanel.setFPSDisplayed(true);
                webcamPanel.setMirrored(false);
                centerPanel.add(webcamPanel, BorderLayout.CENTER);
            } else {
                JLabel noWebcamLabel = new JLabel("No webcam selected", SwingConstants.CENTER);
                noWebcamLabel.setFont(new Font("Arial", Font.BOLD, 18));
                centerPanel.add(noWebcamLabel, BorderLayout.CENTER);
            }
            
            centerPanel.revalidate();
            centerPanel.repaint();
        }
    }

    private void startScanner() {
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, 
                "No webcam available", 
                "Scanner Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!webcam.isOpen()) {
            webcam.open();
        }
        
        webcamPanel.start();
        
        scanResultLabel.setText("Scanning for barcodes...");
        scanStatusLabel.setText("");
        studentInfoLabel.setText("");
        
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::scanBarcode, 0, 100, TimeUnit.MILLISECONDS);
        
        isScanning = true;
    }

    private void stopScanner() {
        if (executor != null) {
            executor.shutdown();
        }
        
        if (webcamPanel != null) {
            webcamPanel.stop();
        }
        
        scanResultLabel.setText("Scanner stopped");
        isScanning = false;
    }

    private void scanBarcode() {
        if(isPropLoc()){
            scanResultLabel.setText("Location verified");
            scanStatusLabel.setForeground(new Color(76, 175, 80)); // Green
        }
        try {
            if (webcam != null && webcam.isOpen()) {
                BufferedImage image = webcam.getImage();
                
                if (image != null) {
                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    
                    try {
                        Result result = new MultiFormatReader().decode(bitmap);
                        String barcodeText = result.getText();
                        
                        // Check for duplicate scans
                        long currentTime = System.currentTimeMillis();
                        if (!barcodeText.equals(lastScannedBarcode) || 
                            (currentTime - lastScanTime) > SCAN_COOLDOWN_MS) {
                            
                            lastScannedBarcode = barcodeText;
                            lastScanTime = currentTime;
                            
                            // Update UI on EDT
                            SwingUtilities.invokeLater(() -> processScannedBarcode(barcodeText));
                        }
                    } catch (NotFoundException ignored) {
                        // No barcode found in this frame, ignore
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                scanResultLabel.setText("Scanner error: " + e.getMessage());
                scanResultLabel.setForeground(Color.RED);
            });
        }
    }

    private void processScannedBarcode(String barcode) {
        scanResultLabel.setText("Barcode detected: " + barcode);
        
        try {
            // Look up student by barcode
            Student student = DatabaseManager.getInstance().getStudentByBarcode(barcode);
            
            if (student != null) {
                // Student found, record attendance
                studentInfoLabel.setText("Student: " + student.getFullName() + " (ID: " + student.getStudentId() + ")");
                
                // Record attendance in database
                boolean recorded = DatabaseManager.getInstance().recordAttendance(
                    student.getBarcode(),
                    new Date(),
                    "PRESENT"
                );
                
                if (recorded) {
                    scanStatusLabel.setText("✓ Attendance recorded successfully");
                    scanStatusLabel.setForeground(new Color(76, 175, 80)); // Green
                    
                    // Play a success sound
                    Toolkit.getDefaultToolkit().beep();
                    
                    // Clear the status after 3 seconds
                    Timer timer = new Timer(3000, e -> {
                        scanStatusLabel.setText("");
                        scanResultLabel.setText("Scanning for barcodes...");
                        studentInfoLabel.setText("");
                    });
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    scanStatusLabel.setText("✗ Failed to record attendance");
                    scanStatusLabel.setForeground(Color.RED);
                }
            } else {
                // Student not found
                studentInfoLabel.setText("No student found with this barcode");
                scanStatusLabel.setText("✗ Unknown barcode");
                scanStatusLabel.setForeground(Color.RED);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            scanStatusLabel.setText("✗ Database error: " + e.getMessage());
            scanStatusLabel.setForeground(Color.RED);
        }
    }

    public void stopCamera() {
        if (isScanning) {
            stopScanner();
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    private boolean isPropLoc(){
        return true;
    }
}