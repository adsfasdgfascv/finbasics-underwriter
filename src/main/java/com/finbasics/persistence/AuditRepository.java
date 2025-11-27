package com.finbasics.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Simple append-only audit log writer.
 */
public class AuditRepository {

    public void log(Integer userId, String action, String details) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO audit_log(user_id, action, details, ts) VALUES(?,?,?,?)")) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, Instant.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            // For now just print; in prod you might escalate.
            e.printStackTrace();
        }
    }
}
