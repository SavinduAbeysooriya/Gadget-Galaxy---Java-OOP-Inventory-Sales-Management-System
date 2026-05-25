package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.SalesRepresentative;
import com.gadgetgalaxy.model.StoreManager;
import com.gadgetgalaxy.model.User;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User database operations.
 * Demonstrates PreparedStatement, Transaction handling, SQL Exception handling, and connection pooling usage.
 */
public class UserDAO implements DAO<User> {

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String fullName = rs.getString("full_name");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        int roleId = rs.getInt("role_id");
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        java.time.LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;
        String status = rs.getString("status");

        if (roleId == 1) {
            return new StoreManager(userId, fullName, username, passwordHash, email, phone, createdAt, status);
        } else {
            return new SalesRepresentative(userId, fullName, username, passwordHash, email, phone, createdAt, status);
        }
    }

    public User findByUsername(String username) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by username: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public User findById(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by ID: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM users";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all users: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean insert(User user) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO users (full_name, username, password_hash, email, phone, role_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setInt(6, user.getRoleId());
            ps.setString(7, user.getStatus());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert user: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(User user) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE users SET full_name = ?, username = ?, password_hash = ?, email = ?, phone = ?, role_id = ?, status = ? WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setInt(6, user.getRoleId());
            ps.setString(7, user.getStatus());
            ps.setInt(8, user.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM users WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }
}
