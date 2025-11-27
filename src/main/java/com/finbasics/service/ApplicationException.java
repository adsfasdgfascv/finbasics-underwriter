package com.finbasics.service;

/**
 * Checked exception for application submission / workflow errors.
 */
public class ApplicationException extends Exception {

    public ApplicationException(String message) { super(message); }

    public ApplicationException(String message, Throwable cause) { super(message, cause); }
}
