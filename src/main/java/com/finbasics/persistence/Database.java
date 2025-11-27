package com.finbasics.persistence;

import com.finbasics.security.PasswordHasher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * Single SQLite database helper.
 * Creates schema under ~/finbasics/finbasics.db and seeds baseline data.
 */
public class Database {

    private static String dbPath;
    private static final String JDBC_PREFIX = "jdbc:sqlite:";

    /**
     * Initialize database: create folder, schema and seed data.
     */
    public static void init() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), "finbasics");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            dbPath = dir.resolve("finbasics.db").toString();

            try (Connection c = getConnection()) {
                // enforce foreign keys
                try (Statement st = c.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON;");
                }
                createSchema(c);
                seedAdmin(c);
                seedPolicies(c);
            }
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Obtain a new connection.
     */
    public static Connection getConnection() throws SQLException {
        if (dbPath == null) {
            throw new IllegalStateException("Database.init() was not called");
        }
        return DriverManager.getConnection(JDBC_PREFIX + dbPath);
    }

    private static void createSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {

            // USERS
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
            """);

            // BASIC POLICIES (for DbCheck and future rule engine)
            st.execute("""
                CREATE TABLE IF NOT EXISTS policies (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
            """);

            // APPLICATIONS
            st.execute("""
                CREATE TABLE IF NOT EXISTS applications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_number TEXT UNIQUE NOT NULL,
                    borrower_type TEXT NOT NULL CHECK (borrower_type IN ('SME','CONSUMER')),
                    borrower_name TEXT NOT NULL,
                    borrower_id_number TEXT NOT NULL,
                    product_type TEXT NOT NULL,
                    requested_amount REAL NOT NULL,
                    status TEXT NOT NULL,
                    sla_hours INTEGER DEFAULT 72,
                    created_by INTEGER NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    FOREIGN KEY (created_by) REFERENCES users(id)
                )
            """);

            // APPLICATION DETAILS (SME vs Consumer fields)
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

            // DOCUMENTS (not used yet, but ready)
            st.execute("""
                CREATE TABLE IF NOT EXISTS documents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL,
                    doc_type TEXT NOT NULL,
                    doc_name TEXT,
                    file_path TEXT,
                    is_required INTEGER DEFAULT 1,
                    is_uploaded INTEGER DEFAULT 0,
                    uploaded_at TEXT,
                    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
                )
            """);

            // TASKS (future "My Tasks" / SLA handling)
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

            // AUDIT LOG
            st.execute("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    action TEXT NOT NULL,
                    details TEXT,
                    ts TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // STATEMENT ANALYSIS (SME vs CONSUMER aware)
            st.execute("""
                CREATE TABLE IF NOT EXISTS statement_analysis (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    application_id INTEGER NOT NULL UNIQUE,
                    borrower_type TEXT NOT NULL CHECK (borrower_type IN ('SME','CONSUMER')),

                    period_start TEXT,
                    period_end   TEXT,

                    revenue            REAL,
                    ebitda             REAL,
                    net_income         REAL,
                    total_assets       REAL,
                    total_liabilities  REAL,
                    current_assets     REAL,
                    current_liabilities REAL,
                    cash               REAL,
                    interest_expense   REAL,
                    debt_service       REAL,

                    ebitda_margin REAL,
                    net_margin    REAL,
                    current_ratio REAL,
                    quick_ratio   REAL,
                    debt_to_equity REAL,
                    dscr          REAL,
                    roa           REAL,
                    roe           REAL,

                    dso               REAL,
                    inventory_turnover REAL,
                    asset_turnover     REAL,

                    monthly_income        REAL,
                    monthly_debt_payments REAL,
                    dti                   REAL,
                    ltv                   REAL,
                    credit_score          INTEGER,

                    created_at TEXT NOT NULL,
                    FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
                )
            """);
        }
    }

    private static void seedAdmin(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            ps.setString(1, "admin");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hash = PasswordHasher.hash("admin123");
                    try (PreparedStatement ins = c.prepareStatement(
                            "INSERT INTO users(username, password_hash, created_at) VALUES(?,?, datetime('now'))")) {
                        ins.setString(1, "admin");
                        ins.setString(2, hash);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    private static void seedPolicies(Connection c) throws SQLException {
        String[][] defaults = {
                {"sme.dscr.min", "1.25"},
                {"sme.current_ratio.min", "1.20"},
                {"consumer.dti.max", "0.45"},
                {"consumer.ltv.max.secured", "0.9"},
                {"pricing.base_rate", "0.04"}
        };

        try (PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO policies(key, value) VALUES(?, ?)")) {
            for (String[] kv : defaults) {
                ps.setString(1, kv[0]);
                ps.setString(2, kv[1]);
                ps.executeUpdate();
            }
        }
    }
}
