package com.finbasics.persistence;

import com.finbasics.model.User;
import java.sql.*;
import java.time.Instant;


/**
 * Repository for performing CRUD operations on `users` table.
 */
public class UserRepository {

    /**
     * Find a user by their username.
     *
     * @param username the username to search for
     * @return a populated {@link com.finbasics.model.User} or {@code null} if not found
     * @throws SQLException when a database error occurs
     */
    public User findByUsername(String username) throws SQLException { // propagate SQLExceptions to caller/service
        // SQL query selecting the fields we need from `users` table
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";

        // try-with-resources ensures Connection and PreparedStatement are closed automatically
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // Bind the username parameter into the SQL (prevents SQL injection)
            ps.setString(1, username);

            // Execute the query and iterate the result set
            try (ResultSet rs = ps.executeQuery()) {
                // If no rows returned, user does not exist
                if (!rs.next()) {
                    return null;
                }

                // Map the single result row into a User object
                User u = new User();
                // Note: model defines setID(Integer) — call it with the int value
                u.setID(rs.getInt("id"));                   // set numeric id
                u.setUsername(rs.getString("username"));    // set username
                u.setPasswordHash(rs.getString("password_hash")); // set stored bcrypt hash
                // created_at is available if you want to set it on the model:
                // u.setCreatedAt(rs.getString("created_at"));
                return u;
            }
        }
    }

    /**
     * Insert a new user and return the generated primary key id.
     *
     * @param username plain username string (should be validated by caller)
     * @param passwordHash hashed password (already hashed by PasswordHasher)
     * @return generated id (or 0 if no id returned)
     * @throws SQLException when a database error occurs
     */
    public int create(String username, String passwordHash) throws SQLException {
        // Keep SQL simple: let DB fill created_at via DEFAULT CURRENT_TIMESTAMP
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Bind parameters in order matching the SQL
            ps.setString(1, username);
            ps.setString(2, passwordHash);

            // Execute insert
            ps.executeUpdate();

            // Retrieve auto-generated key (id)
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

}
