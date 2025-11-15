package com.finbasics.service;

/**
 * AuthException represents authentication/registration related failures.
 * It wraps lower-level errors (like SQL problems) and provides clearer
 * messages to calling code.
 */
public class AuthException extends Exception {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
