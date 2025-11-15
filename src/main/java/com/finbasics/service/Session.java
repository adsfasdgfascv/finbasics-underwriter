package com.finbasics.service;

import com.finbasics.model.User;

/**
 * Session - very small application session holder.
 *
 * Option 2 documentation style: minimal Javadoc for class and methods,
 * with short inline comments for non-obvious lines.
 *
 * PURPOSE: store the currently authenticated user for the running application.
 * DESIGN NOTES:
 * - This is a simple global (static) holder. It is easy to use but has drawbacks
 *   (global mutable state, not thread-safe). For small desktop apps this is often
 *   acceptable; for server apps prefer request-scoped/session-scoped storage.
 */
public class Session {

    // The currently logged-in user. Static so it is accessible from anywhere.
    // Be cautious: static mutable state can make testing and concurrency harder.
    private static User currentUser;

    /**
     * Return the current user, or null if no one is logged in.
     *
     * @return current {@link User} or {@code null}
     */
    public static User getCurrentUser() {
        // Simple getter - returns the shared static reference
        return currentUser;
    }

    /**
     * Set the current user after a successful login.
     *
     * @param u the authenticated user (must not be the user's plain-text password)
     */
    public static void setCurrentUser(User u) {
        // Simple setter - replace the shared reference
        currentUser = u;
    }

    /**
     * Clear the current session (logout).
     * Sets the stored user reference to null.
     */
    public static void clear() {
        // Remove the reference so subsequent calls see no user
        currentUser = null;
    }
}
