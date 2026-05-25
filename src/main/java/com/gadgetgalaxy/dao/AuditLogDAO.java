package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for recording and querying database audit logs.
 */
public class AuditLogDAO {

    /**
     * Inserts a system audit log.
     */
    public boolean insert(Integer userId, String action) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO audit_logs (user_id, action) VALUES (?, ?)";
            ps = conn.prepareStatement(sql);
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, action);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to write audit log: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    /**
     * Fetches all audit logs in detailed text formats for UI or CSV export.
     */
    public List<String[]> findAllLogs() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String[]> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            // Join users to show who did the action
            String sql = "SELECT l.log_id, u.username, l.action, l.log_time " +
                    "FROM audit_logs l LEFT JOIN users u ON l.user_id = u.user_id " +
                    "ORDER BY l.log_time DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String logId = String.valueOf(rs.getInt("log_id"));
                String username = rs.getString("username");
                if (username == null) username = "System / Deleted User";
                String action = rs.getString("action");
                String logTime = rs.getString("log_time");
                list.add(new String[]{logId, username, action, logTime});
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to load audit logs: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }
}
