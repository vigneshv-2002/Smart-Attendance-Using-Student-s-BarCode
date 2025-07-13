package com.attendance.model;

/**
 * Model class representing a system user
 */
public class User {
    private int id;
    private String username;
    private String password; // Note: In memory, this should be handled securely
    private String email;
    private String role; // e.g., "ADMIN", "STAFF"

    public User() {
        // Default constructor
    }

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}