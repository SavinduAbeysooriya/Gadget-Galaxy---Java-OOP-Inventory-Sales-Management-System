package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for product_suppliers many-to-many mapping table.
 */
public class ProductSupplierDAO {

    /** Returns all supplier IDs linked to a product. */
    public List<Integer> findSupplierIdsByProduct(int productId) throws DatabaseException {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT supplier_id FROM product_suppliers WHERE product_id = ?");
            ps.setInt(1, productId);
            rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("supplier_id"));
            return ids;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find product suppliers: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            DBConnection.releaseConnection(conn);
        }
    }

    /** Returns all product IDs linked to a supplier. */
    public List<Integer> findProductIdsBySupplier(int supplierId) throws DatabaseException {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        List<Integer> ids = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT product_id FROM product_suppliers WHERE supplier_id = ?");
            ps.setInt(1, supplierId);
            rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("product_id"));
            return ids;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find supplier products: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            DBConnection.releaseConnection(conn);
        }
    }

    /** Returns all rows as int[]{product_supplier_id, product_id, supplier_id}. */
    public List<int[]> findAll() throws DatabaseException {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        List<int[]> rows = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT product_supplier_id, product_id, supplier_id FROM product_suppliers ORDER BY product_supplier_id");
            rs = ps.executeQuery();
            while (rs.next())
                rows.add(new int[]{ rs.getInt("product_supplier_id"), rs.getInt("product_id"), rs.getInt("supplier_id") });
            return rows;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list product suppliers: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            DBConnection.releaseConnection(conn);
        }
    }

    public boolean insert(int productId, int supplierId) throws DatabaseException {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("INSERT INTO product_suppliers (product_id, supplier_id) VALUES (?, ?)");
            ps.setInt(1, productId);
            ps.setInt(2, supplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert product-supplier link: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            DBConnection.releaseConnection(conn);
        }
    }

    public boolean deleteById(int productSupplierId) throws DatabaseException {
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("DELETE FROM product_suppliers WHERE product_supplier_id = ?");
            ps.setInt(1, productSupplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete product-supplier link: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            DBConnection.releaseConnection(conn);
        }
    }

    public boolean exists(int productId, int supplierId) throws DatabaseException {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement("SELECT 1 FROM product_suppliers WHERE product_id = ? AND supplier_id = ?");
            ps.setInt(1, productId);
            ps.setInt(2, supplierId);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check product-supplier link: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
            DBConnection.releaseConnection(conn);
        }
    }
}
