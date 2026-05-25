package com.gadgetgalaxy.service;

import com.gadgetgalaxy.dao.AuditLogDAO;
import com.gadgetgalaxy.dao.UserDAO;
import com.gadgetgalaxy.exception.AuthenticationException;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.User;
import com.gadgetgalaxy.util.FileUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Handles user authentication, hashing, and session tracking.
 * Demonstrates Service encapsulation, static members, and exception management.
 */
public class AuthenticationService {
    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // Session management: holds the currently logged-in user
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Authenticates credentials and starts user session.
     */
    public User login(String username, String password) throws AuthenticationException, DatabaseException {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!user.getStatus().equalsIgnoreCase("ACTIVE")) {
            throw new AuthenticationException("This account is inactive. Contact the Store Manager.");
        }

        String calculatedHash = hashPassword(password);
        if (!user.getPasswordHash().equals(calculatedHash)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        // Store active session
        currentUser = user;
        
        // Log action to DB & File
        auditLogDAO.insert(user.getUserId(), "User successfully logged in.");
        FileUtil.logAction(user.getUsername(), "LOGIN SUCCESS");

        return user;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        if (currentUser != null) {
            try {
                auditLogDAO.insert(currentUser.getUserId(), "User logged out.");
                FileUtil.logAction(currentUser.getUsername(), "LOGOUT SUCCESS");
            } catch (Exception e) {
                // Silently log error
                System.err.println("Logout log failed: " + e.getMessage());
            }
            currentUser = null;
        }
    }

    /**
     * Simulates secure SHA-256 hashing.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("SHA-256 algorithm missing", ex);
        }
    }
}
