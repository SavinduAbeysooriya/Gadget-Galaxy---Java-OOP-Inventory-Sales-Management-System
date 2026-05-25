package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Inventory;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Inventory operations.
 * Handles stock balances, reorder configurations, and low stock fetches.
 */
public class InventoryDAO implements DAO<Inventory> {

    private Inventory mapResultSetToInventory(ResultSet rs) throws SQLException {
        int id = rs.getInt("inventory_id");
        int productId = rs.getInt("product_id");
        int stock = rs.getInt("quantity_in_stock");
        int reorder = rs.getInt("reorder_level");
        Timestamp updateTs = rs.getTimestamp("last_stock_update");
        java.time.LocalDateTime lastUpdate = updateTs != null ? updateTs.toLocalDateTime() : null;

        return new Inventory(id, productId, stock, reorder, lastUpdate);
    }

    @Override
    public Inventory findById(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM inventory WHERE inventory_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToInventory(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find inventory: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    public Inventory findByProductId(int productId) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM inventory WHERE product_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, productId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToInventory(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find inventory by product ID: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Inventory> findAll() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Inventory> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM inventory";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToInventory(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list inventory: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean insert(Inventory inv) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO inventory (product_id, quantity_in_stock, reorder_level) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, inv.getProductId());
            ps.setInt(2, inv.getQuantityInStock());
            ps.setInt(3, inv.getReorderLevel());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        inv.setInventoryId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert inventory record: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Inventory inv) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE inventory SET quantity_in_stock = ?, reorder_level = ?, last_stock_update = CURRENT_TIMESTAMP WHERE inventory_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, inv.getQuantityInStock());
            ps.setInt(2, inv.getReorderLevel());
            ps.setInt(3, inv.getInventoryId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update inventory record: " + e.getMessage(), e);
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
            String sql = "DELETE FROM inventory WHERE inventory_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete inventory record: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    /**
     * Finds all inventory records that are low in stock (quantity <= reorder level).
     */
    public List<Inventory> findLowStock() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Inventory> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM inventory WHERE quantity_in_stock <= reorder_level";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToInventory(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch low stock items: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }
}
