package com.finbasics;

import java.sql.*;

/**
 * Quick database verification - lists all tables and sample data
 */
public class DbCheck {
    public static void main(String[] args) throws Exception {
        String dbPath = System.getProperty("user.home") + "/finbasics/finbasics.db";
        String url = "jdbc:sqlite:" + dbPath;
        
        try (Connection c = DriverManager.getConnection(url)) {
            // List all tables
            System.out.println("=== DATABASE TABLES ===");
            String query = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(query)) {
                while (rs.next()) {
                    System.out.println("  - " + rs.getString(1));
                }
            }
            
            // Check users table
            System.out.println("\n=== USERS TABLE ===");
            query = "SELECT id, username, created_at FROM users";
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(query)) {
                while (rs.next()) {
                    System.out.println("  ID: " + rs.getInt(1) + ", Username: " + rs.getString(2) + ", Created: " + rs.getString(3));
                }
            }
            
            // Check policies table
            System.out.println("\n=== POLICIES TABLE ===");
            query = "SELECT key, value FROM policies LIMIT 5";
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(query)) {
                while (rs.next()) {
                    System.out.println("  " + rs.getString(1) + " = " + rs.getString(2));
                }
            }
            
            System.out.println("\n✓ Database is working!");
        }
    }
}
