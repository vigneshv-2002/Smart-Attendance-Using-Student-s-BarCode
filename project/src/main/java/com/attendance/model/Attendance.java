package com.attendance.model;

import java.util.Date;

/**
 * Model class representing an attendance record
 */
public class Attendance {
    private int id;
    private String studentId;
    private Date scanTime;
    private String status; // e.g., "PRESENT", "LATE", "ABSENT"
    private Student student; // For reference to student details

    public Attendance() {
        // Default constructor
    }

    public Attendance(String studentId, Date scanTime, String status) {
        this.studentId = studentId;
        this.scanTime = scanTime;
        this.status = status;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Date getScanTime() {
        return scanTime;
    }

    public void setScanTime(Date scanTime) {
        this.scanTime = scanTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", scanTime=" + scanTime +
                ", status='" + status + '\'' +
                '}';
    }
}