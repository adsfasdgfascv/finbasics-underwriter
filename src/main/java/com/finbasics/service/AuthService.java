package com.finbasics.service;

import com.finbasics.model.User;
import com.finbasics.persistence.UserRepository;
import com.finbasics.persistence.AuditRepository;
import com.finbasics.security.PasswordHasher;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Service responsible for authentication and registration logic.
 *
 * Improvements:
 * - Validates inputs
 * - Converts SQL exceptions into {@link AuthException}
 * - Detects unique-constraint violations and returns a friendly message
 */

public class AuthService {
    private final UserRepository userRepo = new UserRepository();
    private final AuditRepository audit = new AuditRepository();

    private static final Pattern USERNAME_ALLOWED = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    public User login(String username, String password) throws AuthException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthException("Username and password are required");
        }

        try {
            // Look up the user by username using the repository instance declared above.
            User u = userRepo.findByUsername(username.trim());

            // If user is not found or password doesn't match, record a failed login and throw.
            if (u == null || !PasswordHasher.matches(password, u.getPasswordHash())) {
                audit.log(null, "LOGIN_FAIL", "username=" + username);
                throw new AuthException("Invalid username or password");
            }

            // Successful login: write audit and set session.
            audit.log(u.getId(), "LOGIN_SUCCESS", "user=" + username);
            Session.setCurrentUser(u);
            return u;
        } catch (SQLException e) {
            throw new AuthException("Database error during login", e);
        }
    }

    public User register(String username, String password) throws AuthException {
        // Basic validation
        if (username == null || username.isBlank()) {
            throw new AuthException("Username is required");
        }
        String trimmed = username.trim();
        if (!USERNAME_ALLOWED.matcher(trimmed).matches()) {
            throw new AuthException("Username must be 3-50 characters and contain only letters, digits, '.', '_' or '-'");
        }
        if (password == null || password.length() < 8) {
            throw new AuthException("Password must be at least 8 characters");
        }

        String hashed = PasswordHasher.hash(password);

        try {
            // Insert and get generated id
            int id = userRepo.create(trimmed, hashed);

            // Construct User object from available info (avoid an extra DB round-trip)
            User u = new User();
            u.setID(id);
            u.setUsername(trimmed);
            u.setPasswordHash(hashed);

            audit.log(u.getId(), "REGISTER", "user=" + trimmed);
            Session.setCurrentUser(u);
            return u;
        } catch (SQLException e) {
            // Detect unique-constraint violation in SQLite messages
            String msg = e.getMessage() != null && e.getMessage().toUpperCase().contains("UNIQUE")
                    ? "Username already exists"
                    : "Database error during registration";
            throw new AuthException(msg, e);
        }
    }

    public void logout(){
        var u = Session.getCurrentUser();
        audit.log(u != null ? u.getId() : null, "LOGOUT", "");
        Session.clear();
    }
}
