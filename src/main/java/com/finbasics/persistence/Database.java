package com.finbasics.persistence;

// Import statements: These bring in pre-built Java libraries we need for database operations
import java.nio.file.Files;         // For file system operations
import java.nio.file.Path;          // For representing file paths
import java.nio.file.Paths;         // For creating path objects
import java.sql.*;                  // For SQL database operations (Connection, Statement, ResultSet, etc.)
import java.time.Instant;           // For getting current timestamp
import com.finbasics.security.PasswordHasher;  // Our custom class to hash passwords securely

/**
 * DATABASE CLASS - Manages all SQLite database operations for the FinBasics application.
 * 
 * PURPOSE: This class handles:
 * 1. Creating the SQLite database file at ~/finbasics/finbasics.db
 * 2. Creating all required database tables (users, borrowers, sessions, etc.)
 * 3. Seeding initial data (default admin user and policy settings)
 * 4. Providing a way to get database connections for other parts of the app
 * 
 * DESIGN NOTE: No user roles - all users are company employees with equal access
 * 
 * FLOW: When the app starts → Database.init() is called → Tables are created → Default data is inserted
 */
public class Database {
    
    // Variable to store the full path to the SQLite database file
    // Example: C:\Users\YourName\finbasics\finbasics.db
    private static String dbPath;
    
    // The prefix required by SQLite JDBC driver to connect to a local database file
    // "jdbc:sqlite:" tells Java to use SQLite instead of other databases like MySQL
    private static final String JDBC_PREFIX = "jdbc:sqlite:";

    /**
     * INIT METHOD - Initialize the entire database
     * 
     * WHAT IT DOES:
     * 1. Creates the ~/finbasics folder if it doesn't exist
     * 2. Sets up the path to finbasics.db
     * 3. Gets a connection to the database
     * 4. Creates all tables if they don't already exist
     * 5. Inserts a default admin user
     * 6. Sets up default policy values
     * 
     * WHEN TO CALL: Only once when the application starts (in App.java main() or start())
     */
    public static void init() {
        try {
            // Step 1: Build the folder path: [your home directory]/finbasics
            // Example: C:\Users\gabie\finbasics
            Path dir = Paths.get(System.getProperty("user.home"), "finbasics");
            
            // Step 2: Check if the folder exists
            if (!Files.exists(dir)) {
                // If folder doesn't exist, create it (and any parent folders needed)
                Files.createDirectories(dir);
            }
            
            // Step 3: Build the full database file path
            // Example: C:\Users\gabie\finbasics\finbasics.db
            dbPath = dir.resolve("finbasics.db").toString();
            
            // Step 4: Get a connection to the database (creates it if it doesn't exist)
            try (Connection c = getConnection()) {
                // Step 5: Enable foreign keys - this enforces relationship rules between tables
                // Foreign keys ensure data integrity (e.g., can't reference a user that doesn't exist)
                c.createStatement().execute("PRAGMA foreign_keys = ON;");
                
                // Step 6: Create all database tables
                createSchema(c);
                
                // Step 7: Insert default admin user (username: admin, password: admin123)
                seedAdmin(c);
                
                // Step 8: Insert default policy settings (min credit ratio, DSCR, etc.)
                seedPolicies(c);
            }
        } catch (Exception e) {
            // If anything goes wrong, throw an error with a helpful message
            throw new RuntimeException("DB failed to initialize: " + e.getMessage(), e);
        }
    }

    /**
     * GET CONNECTION METHOD - Returns a database connection
     * 
     * WHAT IT DOES: Creates and returns a new connection to the SQLite database
     * 
     * WHY: Other parts of the app use this to get a connection when they need to:
     * - Query the database (SELECT)
     * - Insert data (INSERT)
     * - Update data (UPDATE)
     * - Delete data (DELETE)
     * 
     * USAGE: Connection conn = Database.getConnection();
     */
    public static Connection getConnection() throws SQLException {
        // Connect to SQLite using the database file path
        // Format: jdbc:sqlite:[path to db file]
        return DriverManager.getConnection(JDBC_PREFIX + dbPath);
    }

    /**
     * CREATE SCHEMA METHOD - Creates all database tables
     * 
     * WHAT IT DOES: Defines the structure of all database tables
     * Each table is a spreadsheet-like structure with columns and data types
     * 
     * TABLES CREATED:
     * - users: Stores login credentials for app users
     * - borrowers: Stores company/individual information being analyzed
     * - sessions: Stores import sessions (each file upload is a session)
     * - statement_lines: Stores financial line items from imports
     * - analysis_runs: Stores financial analysis results
     * - policies: Stores configuration settings
     * - audit_log: Tracks who did what and when (for compliance)
     */
    private static void createSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            
            // ===== TABLE 1: USERS =====
            // Purpose: Store login credentials for company employees
            // NOTE: No roles - all employees have equal access to company data
            // Columns:
            //   - id: Unique identifier (auto-generated)
            //   - username: Login name (must be unique)
            //   - password_hash: Encrypted password (NOT plain text for security)
            //   - created_at: When the user was created
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // ===== TABLE 2: BORROWERS =====
            // Purpose: Store information about companies/individuals being analyzed
            // Example: Bank of America, ABC Corp, John Smith, etc.
            // Columns:
            //   - id: Unique identifier
            //   - name: Company or individual name
            //   - created_at: When this record was added
            st.execute("""
                CREATE TABLE IF NOT EXISTS borrowers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
            """);

            // ===== TABLE 3: SESSIONS =====
            // Purpose: Store import sessions (each time user uploads a file = one session)
            // Think of it as: "Who uploaded what file, when, and how was it mapped?"
            // Columns:
            //   - id: Unique identifier
            //   - borrower_id: Which company does this session belong to? (links to borrowers table)
            //   - period: Time period of financial data (e.g., "2023-Q1")
            //   - source_file: Name of uploaded file
            //   - mapping_json: JSON storing how columns were mapped
            //   - created_by: Which user created this session? (links to users table)
            //   - created_at: Timestamp
            st.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    borrower_id INTEGER NOT NULL,
                    period TEXT,
                    source_file TEXT,
                    mapping_json TEXT,
                    created_by INTEGER,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (borrower_id) REFERENCES borrowers(id) ON DELETE CASCADE,
                    FOREIGN KEY (created_by) REFERENCES users(id)
                )
            """);

            // ===== TABLE 4: STATEMENT_LINES =====
            // Purpose: Store extracted financial metrics from each session
            // Example: "Revenue: $1,000,000", "Expenses: $500,000", etc.
            // Columns:
            //   - id: Unique identifier
            //   - session_id: Which session does this belong to? (links to sessions table)
            //   - metric: Name of financial metric (e.g., "Revenue", "EBITDA")
            //   - amount: The dollar value
            //   - period: Time period (e.g., "2023-Q1")
            st.execute("""
                CREATE TABLE IF NOT EXISTS statement_lines (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id INTEGER NOT NULL,
                    metric TEXT NOT NULL,
                    amount REAL NOT NULL,
                    period TEXT,
                    UNIQUE(session_id, metric, period),
                    FOREIGN KEY(session_id) REFERENCES sessions(id) ON DELETE CASCADE
                )
            """);

            // ===== TABLE 5: ANALYSIS_RUNS =====
            // Purpose: Store results of financial analyses (ratios, DSCR, etc.)
            // Example: "Debt Service Coverage Ratio: 1.5x", "Current Ratio: 2.3x"
            // Columns:
            //   - id: Unique identifier
            //   - borrower_id: Which company was analyzed?
            //   - session_id: Which data was used?
            //   - type: Type of analysis ("DSCR", "RATIOS", "AFN", "RATE")
            //   - inputs_json: Input parameters used for analysis
            //   - outputs_json: Results of the analysis
            //   - user_id: Who ran this analysis?
            //   - ts: Timestamp
            st.execute("""
                CREATE TABLE IF NOT EXISTS analysis_runs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    borrower_id INTEGER,
                    session_id INTEGER,
                    type TEXT NOT NULL,
                    inputs_json TEXT,
                    outputs_json TEXT,
                    user_id INTEGER,
                    ts TEXT NOT NULL,
                    FOREIGN KEY(borrower_id) REFERENCES borrowers(id) ON DELETE SET NULL,
                    FOREIGN KEY(session_id) REFERENCES sessions(id) ON DELETE SET NULL,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE SET NULL
                )
            """);

            // ===== TABLE 6: POLICIES =====
            // Purpose: Store configuration settings for the app
            // Example: "CR_MIN=1.50" means "Minimum Credit Ratio is 1.50"
            // These are global settings that all analyses use
            // Columns:
            //   - id: Unique identifier
            //   - key: Setting name (e.g., "CR_MIN", "DSCR_MIN")
            //   - value: Setting value (e.g., "1.50", "1.20")
            //   - updated_at: When was this setting last changed?
            st.execute("""
                CREATE TABLE IF NOT EXISTS policies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    key TEXT UNIQUE NOT NULL,
                    value TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """);

            // ===== TABLE 7: AUDIT_LOG =====
            // Purpose: Track all important actions (who did what, when)
            // This is for compliance and debugging
            // Example: "User john_doe deleted borrower ABC Corp on 2025-01-15 14:30:00"
            // Columns:
            //   - id: Unique identifier
            //   - user_id: Which user performed the action?
            //   - action: What action (e.g., "CREATED_SESSION", "DELETED_BORROWER")
            //   - details: Additional details about what happened
            //   - ts: Timestamp
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
        }
    }

    /**
     * SEED ADMIN METHOD - Insert a default admin user into the database
     * 
     * WHAT IT DOES:
     * 1. Checks if admin user already exists
     * 2. If not, creates one with:
     *    - Username: "admin"
     *    - Password: "admin123" (hashed securely using BCrypt)
     * 
     * WHY: New installations need at least one user to log in with
     * NOTE: This is an employee user with full company access - no role differentiation exists
     * 
     * SECURITY: Password is hashed using PasswordHasher.hash() so it's NOT stored as plain text
     */
    private static void seedAdmin(Connection c) throws SQLException {
        // Step 1: Create a prepared statement to COUNT how many users have username "admin"
        // A "prepared statement" is a safe way to query the database (prevents SQL injection attacks)
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            // Step 2: Set the parameter - replace ? with "admin"
            ps.setString(1, "admin");
            
            // Step 3: Execute the query and get the result
            try (ResultSet rs = ps.executeQuery()) {
                // Step 4: Move to the first (and only) row of results
                rs.next();
                
                // Step 5: Get the count value
                // If count is 0, admin user doesn't exist yet, so create it
                if (rs.getInt(1) == 0) {
                    // Step 6: Hash the password "admin123" using BCrypt encryption
                    // This ensures the password is never stored in plain text
                    String hash = PasswordHasher.hash("admin123");
                    
                    // Step 7: Prepare an INSERT statement to add the admin user
                    try (PreparedStatement insertPs = c.prepareStatement(
                            "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)")) {
                        // Step 8: Set the parameters
                        insertPs.setString(1, "admin");           // Username = "admin"
                        insertPs.setString(2, hash);              // Password (hashed) = hashed version of "admin123"
                        insertPs.setString(3, Instant.now().toString());  // Current timestamp
                        
                        // Step 9: Execute the INSERT - this adds the row to the database
                        insertPs.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * SEED POLICIES METHOD - Insert default policy settings into the database
     * 
     * WHAT IT DOES: Sets up default values for financial thresholds:
     * - CR_MIN: Minimum Current Ratio (1.50 means assets must be 1.5x liabilities)
     * - TIE_MIN: Times Interest Earned minimum (3.00 means must cover interest 3x over)
     * - DSCR_MIN: Debt Service Coverage Ratio minimum (1.20 means must cover debt payments 1.2x over)
     * 
     * WHY: The app uses these settings to determine if a borrower is financially healthy
     */
    private static void seedPolicies(Connection c) throws SQLException {
        // Call upsertPolicy for each policy setting
        // "Upsert" = "Update if exists, or Insert if not exists"
        
        // Set minimum Current Ratio to 1.50
        upsertPolicy(c, "CR_MIN", "1.50");
        
        // Set minimum Times Interest Earned to 3.00
        upsertPolicy(c, "TIE_MIN", "3.00");
        
        // Set minimum DSCR to 1.20
        upsertPolicy(c, "DSCR_MIN", "1.20");
    }

    /**
     * UPSERT POLICY METHOD - Insert or Update a policy setting
     * 
     * WHAT IT DOES:
     * 1. Tries to INSERT a new policy
     * 2. If a policy with that key already exists, REPLACE it instead of crashing
     * 3. Either way, the policy gets updated to the new value
     * 
     * PARAMETERS:
     * - c: Database connection
     * - key: The setting name (e.g., "CR_MIN")
     * - value: The setting value (e.g., "1.50")
     * 
     * WHY: Allows policies to be updated without checking if they exist first
     */
    private static void upsertPolicy(Connection c, String key, String value) throws SQLException {
        // Prepare an "INSERT OR REPLACE" statement
        // This means: "Insert this row, but if a row with the same key exists, replace it"
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT OR REPLACE INTO policies (key, value, updated_at) VALUES (?, ?, ?)")) {
            
            // Step 1: Set the key (policy name)
            ps.setString(1, key);
            
            // Step 2: Set the value (policy value)
            ps.setString(2, value);
            
            // Step 3: Set the updated timestamp to right now
            ps.setString(3, Instant.now().toString());
            
            // Step 4: Execute the INSERT OR REPLACE statement
            ps.executeUpdate();
        }
    }
}


