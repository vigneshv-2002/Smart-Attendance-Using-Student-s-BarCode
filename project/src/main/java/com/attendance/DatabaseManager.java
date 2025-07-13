package com.attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.attendance.model.Attendance;
import com.attendance.model.Student;
import com.attendance.model.User;

/**
 * Singleton class for managing database connections and operations
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private final String DB_URL = "jdbc:sqlite:attendance.db";

    private DatabaseManager() {
        // Private constructor for singleton pattern
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialize the database with required tables
     */
    public void initDatabase() throws SQLException {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create database connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Create tables if they don't exist
            createTables();
            
            // Check if admin user exists, if not create default admin
            createDefaultAdmin();
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    private void createTables() throws SQLException {
        String[] createTableSQL = {
            // Users table for authentication
            "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Students table 
            "CREATE TABLE IF NOT EXISTS students (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id TEXT NOT NULL UNIQUE," +
                "barcode TEXT NOT NULL UNIQUE," +
                "first_name TEXT NOT NULL," +
                "last_name TEXT NOT NULL," +
                "email TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            
            // Attendance records table
            "CREATE TABLE IF NOT EXISTS attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_id TEXT NOT NULL," +
                "scan_time TIMESTAMP NOT NULL," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY (student_id) REFERENCES students(student_id)" +
            ")"
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String sql : createTableSQL) {
                stmt.execute(sql);
            }
        }
    }

    private void createDefaultAdmin() throws SQLException {
        String checkAdminSQL = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkAdminSQL)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // No admin exists, create default admin user (username: admin, password: admin123)
                String createAdminSQL = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(createAdminSQL)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123"); // In production, this should be hashed
                    pstmt.setString(3, "admin@school.edu");
                    pstmt.setString(4, "ADMIN");
                    pstmt.executeUpdate();
                }
            }
        }
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, password comparison should be done with hashes
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        }
        return null; // Authentication failed
    }

    /**
     * Add a new student
     */
    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_id, barcode, first_name, last_name, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getBarcode());
            pstmt.setString(3, student.getFirstName());
            pstmt.setString(4, student.getLastName());
            pstmt.setString(5, student.getEmail());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Get student by barcode
     */
    public Student getStudentByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM students WHERE barcode = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, barcode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setId(rs.getInt("id"));
                    student.setStudentId(rs.getString("student_id"));
                    student.setBarcode(rs.getString("barcode"));
                    student.setFirstName(rs.getString("first_name"));
                    student.setLastName(rs.getString("last_name"));
                    student.setEmail(rs.getString("email"));
                    return student;
                }
            }
        }
        return null;
    }

    /**
     * Record attendance
     */
    public boolean recordAttendance(String studentId, Date scanTime, String status) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, scan_time, status) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setTimestamp(2, new Timestamp(scanTime.getTime()));
            pstmt.setString(3, status);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Get all students
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setStudentId(rs.getString("student_id"));
                student.setBarcode(rs.getString("barcode"));
                student.setFirstName(rs.getString("first_name"));
                student.setLastName(rs.getString("last_name"));
                student.setEmail(rs.getString("email"));
                students.add(student);
            }
        }
        
        return students;
    }

    /**
     * Get attendance records for a specific date
     */
    public List<Attendance> getAttendanceByDate(Date date) throws SQLException {
    List<Attendance> attendanceList = new ArrayList<>();

    // Define start and end of the day
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    Timestamp startOfDay = new Timestamp(cal.getTimeInMillis());

    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    Timestamp endOfDay = new Timestamp(cal.getTimeInMillis());

    String sql = "SELECT a.id, a.student_id, a.scan_time, a.status, " +
                 "s.first_name, s.last_name " +
                 "FROM attendance a " +
                 "JOIN students s ON a.student_id = s.barcode " +
                 "WHERE a.scan_time BETWEEN ? AND ? " +
                 "ORDER BY a.scan_time DESC";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setTimestamp(1, startOfDay);
        pstmt.setTimestamp(2, endOfDay);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setId(rs.getInt("id"));
                attendance.setStudentId(rs.getString("student_id"));
                attendance.setScanTime(rs.getTimestamp("scan_time"));
                attendance.setStatus(rs.getString("status"));

                Student student = new Student();
                student.setStudentId(rs.getString("student_id"));
                student.setFirstName(rs.getString("first_name"));
                student.setLastName(rs.getString("last_name"));
                attendance.setStudent(student);

                attendanceList.add(attendance);
            }
        }
    }

    return attendanceList;
}


    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}