package com.gadgetgalaxy.util;

import com.gadgetgalaxy.exception.DatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages database connections using a simple simulation of a Connection Pool.
 * Demonstrates the use of the static keyword, synchronization, queues, and exception handling.
 */
public class DBConnection {
    // Database settings
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "gadget_galaxy_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE 
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Pool size
    private static final int INITIAL_POOL_SIZE = 5;
    private static final Queue<Connection> connectionPool = new LinkedList<>();

    // Static initializer block to load the JDBC driver and pre-populate the connection pool
    static {
        try {
            Class.forName(DRIVER);
            initializePool();
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection pool: " + e.getMessage());
        }
    }

    // Private constructor to prevent instantiation (Utility class)
    private DBConnection() {}

    /**
     * Fills the connection pool with active JDBC connections.
     */
    private static void initializePool() throws SQLException {
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            connectionPool.offer(createNewConnection());
        }
        System.out.println("Connection pool initialized with " + INITIAL_POOL_SIZE + " connections.");
    }

    /**
     * Establishes a new JDBC connection.
     */
    private static Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Borrows an active connection from the pool.
     * If the pool is empty, creates a new connection on demand.
     */
    public static synchronized Connection getConnection() throws DatabaseException {
        try {
            Connection connection = connectionPool.poll();
            if (connection == null) {
                return createNewConnection();
            }
            // Check if connection is still valid, else create a new one
            if (connection.isClosed() || !connection.isValid(2)) {
                return createNewConnection();
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to acquire database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a connection to the pool.
     */
    public static synchronized void releaseConnection(Connection connection) {
        if (connection == null) return;
        try {
            if (!connection.isClosed() && connection.isValid(2)) {
                // Return to pool if size is within limits
                if (connectionPool.size() < INITIAL_POOL_SIZE) {
                    connectionPool.offer(connection);
                } else {
                    connection.close(); // Close surplus connections
                }
            }
        } catch (SQLException e) {
            System.err.println("Error closing surplus or invalid connection: " + e.getMessage());
        }
    }

    /**
     * Closes all connections in the pool (useful during system shutdown).
     */
    public static synchronized void shutdown() {
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Silently ignore during shutdown
            }
        }
        System.out.println("Connection pool shut down successfully.");
    }
}
