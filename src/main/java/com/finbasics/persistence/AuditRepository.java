package com.finbasics.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

/**
 * AuditRepository - simple helper to write audit_log entries.
 *
 * Option 2: Minimal Javadoc + concise inline comments for important lines.
 */
public class AuditRepository {

    /**
     * Append an audit log record.
     *
     * @param userId  id of the acting user, or null for system actions
     * @param action  short action code or description
     * @param details optional human-readable details (may be null)
     */
    public void log(Integer userId, String action, String details) {
        // SQL with placeholders (?): values bound later to avoid SQL injection
        String sql = "INSERT INTO audit_log(user_id, action, details, ts) VALUES(?, ?, ?, ?)";

        // try-with-resources: ensures Connection and PreparedStatement are closed automatically
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // Bind userId (may be null)
            if (userId == null) {
                // set NULL for integer column
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
            }

            // Bind remaining parameters: action, details, timestamp
            ps.setString(2, action); // short action code
            ps.setString(3, details); // longer details (may be null)
            ps.setString(4, Instant.now().toString()); // iso timestamp

            // Execute the insert
            ps.executeUpdate();

        } catch (SQLException e) {
            // Basic error handling for now: print stack trace so developer can see failures
            // In production you'd write to a logger and possibly retry or escalate
            e.printStackTrace();
        }
    }
}
