package com.finbasics.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing and verification utilities.
 *
 * This version provides both verify(...) and matches(...) so that
 * existing AuthService code that calls PasswordHasher.matches(...)
 * continues to compile and work.
 */
public class PasswordHasher {

    // Work factor (cost). 10 is a reasonable default for desktop.
    private static final int WORK_FACTOR = 10;

    /**
     * Hash a plain-text password using BCrypt.
     *
     * @param plainPassword raw password entered by the user
     * @return BCrypt hash string to store in the database
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verify a plain-text password against a BCrypt hash.
     *
     * @param plainPassword raw password
     * @param hash          previously stored BCrypt hash
     * @return true if they match, false otherwise
     */
    public static boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hash);
    }

    /**
     * Backward-compatible alias for verify(...).
     * Your existing AuthService calls PasswordHasher.matches(...),
     * so we keep this method to avoid compilation errors.
     *
     * @param plainPassword raw password
     * @param hash          stored hash
     * @return true if password matches hash
     */
    public static boolean matches(String plainPassword, String hash) {
        return verify(plainPassword, hash);
    }
}
