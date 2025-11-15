package com.finbasics.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 */
public class PasswordHasher {

    /**
     * Hash a plaintext password using BCrypt with a cost factor of 10.
     *
     * @param plain the plaintext password
     * @return the hashed password
     */
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    /**
     * Verify a plaintext password against a BCrypt hash.
     *
     * @param plain the plaintext password
     * @param hashed the hashed password
     * @return true if the password matches, false otherwise
     */
    public static boolean matches(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }
}
