package com.finbasics.model;

/**
 * Simple User entity matching the "users" table:
 *
 *  id            INTEGER PRIMARY KEY AUTOINCREMENT
 *  username      TEXT UNIQUE NOT NULL
 *  password_hash TEXT NOT NULL
 *  created_at    TEXT NOT NULL
 *
 * NOTE: This class also exposes legacy method names (setID, getusername)
 * to remain compatible with existing code in UserRepository, AuthService,
 * and LoginController.
 */
public class User {

    private int id;
    private String username;
    private String passwordHash;
    private String createdAt;

    public User() {
    }

    public User(int id, String username, String passwordHash, String createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // -------- id --------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Legacy alias for setId(int) used by older code (setID).
     */
    public void setID(int id) {
        this.id = id;
    }

    // -------- username --------

    /**
     * Username used for login and display (e.g. "admin").
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Legacy alias for getUsername() used by older code (getusername).
     */
    public String getusername() {
        return username;
    }

    // -------- password hash --------

    /**
     * BCrypt hash stored in the database.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // -------- created_at --------

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
