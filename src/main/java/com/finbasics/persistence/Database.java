package com.finbasics.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import com.finbasics.security.PasswordHasher;

public class Database {
    
    private static String dbPath;
    private static final String JDBC_PREFIX = "jdbc:sqlite:";

    public static void init() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), "finbasics");
            if (!Files.exists(dir)) Files.createDirectories(dir);
            dbPath = dir.resolve("finbasics.db").toString();
            
            try (Connection c = getConnection()) {
                c.createStatement().execute("PRAGMA foreign_keys = ON;");
                createSchema(c);
                seedAdmin(c);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB failed to initialize", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_PREFIX + dbPath);
    }

    private static void createSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            
            // 1. Users
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // 2. Applications (The Core Table)
            st.execute("""
                CREATE TABLE IF NOT EXISTS applications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_number TEXT UNIQUE NOT NULL,
                    borrower_type TEXT NOT NULL CHECK(borrower_type IN ('SME', 'CONSUMER')),
                    borrower_name TEXT NOT NULL,
                    borrower_id_number TEXT NOT NULL,
                    product_type TEXT NOT NULL,
                    requested_amount REAL NOT NULL,
                    status TEXT NOT NULL DEFAULT 'INTAKE',
                    sla_hours INTEGER DEFAULT 72,
                    created_by INTEGER NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY (created_by) REFERENCES users(id)
                )
            """);

            // 3. Application Details (Extended Info)
            st.execute("""
                CREATE TABLE IF NOT EXISTS application_details (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL UNIQUE,
                    business_name TEXT,
                    ein TEXT,
                    naics_code TEXT,
                    date_established TEXT,
                    guarantor_name TEXT,
                    consumer_name TEXT,
                    ssn TEXT,
                    employer TEXT,
                    annual_income REAL,
                    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
                )
            """);

            // 4. Documents (Tracking uploads)
            st.execute("""
                CREATE TABLE IF NOT EXISTS documents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL,
                    doc_type TEXT NOT NULL,
                    doc_name TEXT,
                    file_path TEXT,
                    is_required BOOLEAN DEFAULT 1,
                    is_uploaded BOOLEAN DEFAULT 0,
                    uploaded_at TEXT,
                    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
                )
            """);

            // 5. Tasks (Workflow)
            st.execute("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    priority TEXT DEFAULT 'NORMAL',
                    status TEXT DEFAULT 'PENDING',
                    due_date TEXT,
                    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
                )
            """);
            
            // 6. Audit Log
            st.execute("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action TEXT NOT NULL,
                    details TEXT,
                    ts TEXT NOT NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                )
            """);

            
            // 7. Statement Analysis (one summary row per application)
            st.execute("""
                CREATE TABLE IF NOT EXISTS statement_analysis (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL UNIQUE,
                    period_start TEXT,
                    period_end TEXT,
                    revenue REAL,
                    ebitda REAL,
                    net_income REAL,
                    total_assets REAL,
                    total_liabilities REAL,
                    current_assets REAL,
                    current_liabilities REAL,
                    cash REAL,
                    interest_expense REAL,
                    debt_service REAL,
                    ebitda_margin REAL,
                    net_margin REAL,
                    current_ratio REAL,
                    quick_ratio REAL,
                    debt_to_equity REAL,
                    dscr REAL,
                    roa REAL,
                    roe REAL,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
                )
            """);
        }
    }

    private static void seedAdmin(Connection c) throws SQLException {
        // (Keep your existing admin seeding logic here)
         try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            ps.setString(1, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hash = PasswordHasher.hash("admin123");
                    c.createStatement().execute("INSERT INTO users (username, password_hash) VALUES ('admin', '" + hash + "')");
                }
            }
        }
    }
}