package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.exception.InsufficientStockException;
import com.gadgetgalaxy.model.Sale;
import com.gadgetgalaxy.model.SaleItem;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Sale and SaleItem database operations.
 * Implements transaction management (commit/rollback) and stock checks.
 */
public class SalesDAO implements DAO<Sale> {

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        int id = rs.getInt("sale_id");
        String invoice = rs.getString("invoice_no");
        Integer customer = rs.getInt("customer_id");
        if (rs.wasNull()) customer = null;
        int soldBy = rs.getInt("sold_by");
        Timestamp dateTs = rs.getTimestamp("sale_date");
        LocalDateTime date = dateTs != null ? dateTs.toLocalDateTime() : null;
        double amount = rs.getDouble("total_amount");
        String method = rs.getString("payment_method");
        String status = rs.getString("sale_status");

        return new Sale(id, invoice, customer, soldBy, date, amount, method, status);
    }

    private SaleItem mapResultSetToSaleItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("sale_item_id");
        int saleId = rs.getInt("sale_id");
        int productId = rs.getInt("product_id");
        int qty = rs.getInt("quantity");
        double price = rs.getDouble("unit_price");
        double subtotal = rs.getDouble("subtotal");

        return new SaleItem(id, saleId, productId, qty, price, subtotal);
    }

    @Override
    public Sale findById(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM sales WHERE sale_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToSale(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sale: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    public Sale findByInvoiceNo(String invoiceNo) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM sales WHERE invoice_no = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, invoiceNo);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToSale(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find sale by invoice: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Sale> findAll() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Sale> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToSale(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list sales: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean insert(Sale entity) throws DatabaseException {
        // Implement simple single-insert just in case, but standard execution should use processSaleTransaction
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO sales (invoice_no, customer_id, sold_by, total_amount, payment_method, sale_status) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getInvoiceNo());
            if (entity.getCustomerId() != null) {
                ps.setInt(2, entity.getCustomerId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, entity.getSoldBy());
            ps.setDouble(4, entity.getTotalAmount());
            ps.setString(5, entity.getPaymentMethod());
            ps.setString(6, entity.getSaleStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) entity.setSaleId(gk.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert sale record: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Sale entity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE sales SET invoice_no = ?, customer_id = ?, sold_by = ?, total_amount = ?, payment_method = ?, sale_status = ? WHERE sale_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, entity.getInvoiceNo());
            if (entity.getCustomerId() != null) {
                ps.setInt(2, entity.getCustomerId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, entity.getSoldBy());
            ps.setDouble(4, entity.getTotalAmount());
            ps.setString(5, entity.getPaymentMethod());
            ps.setString(6, entity.getSaleStatus());
            ps.setInt(7, entity.getSaleId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update sale record: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM sales WHERE sale_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete sale record: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    public List<SaleItem> findItemsBySaleId(int saleId) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<SaleItem> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM sale_items WHERE sale_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, saleId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToSaleItem(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list sale items: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    /**
     * Records a Sale and its sub-items, and updates the inventory, all within a transaction.
     */
    public boolean processSaleTransaction(Sale sale, List<SaleItem> items) throws DatabaseException, InsufficientStockException {
        Connection conn = null;
        PreparedStatement psSale = null;
        PreparedStatement psItem = null;
        PreparedStatement psStockCheck = null;
        PreparedStatement psStockDeduct = null;
        ResultSet rsKeys = null;
        ResultSet rsStock = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Turn off auto commit for transaction safety

            // 1. Stock validation
            for (SaleItem item : items) {
                String checkSql = "SELECT quantity_in_stock FROM inventory WHERE product_id = ?";
                psStockCheck = conn.prepareStatement(checkSql);
                psStockCheck.setInt(1, item.getProductId());
                rsStock = psStockCheck.executeQuery();
                if (rsStock.next()) {
                    int stock = rsStock.getInt("quantity_in_stock");
                    if (stock < item.getQuantity()) {
                        throw new InsufficientStockException("Insufficient stock for product ID " + item.getProductId(), stock, item.getQuantity());
                    }
                } else {
                    throw new DatabaseException("Product not found in inventory: ID " + item.getProductId());
                }
                rsStock.close();
                psStockCheck.close();
            }

            // 2. Insert Sale
            String saleSql = "INSERT INTO sales (invoice_no, customer_id, sold_by, total_amount, payment_method, sale_status) VALUES (?, ?, ?, ?, ?, ?)";
            psSale = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            psSale.setString(1, sale.getInvoiceNo());
            if (sale.getCustomerId() != null) {
                psSale.setInt(2, sale.getCustomerId());
            } else {
                psSale.setNull(2, Types.INTEGER);
            }
            psSale.setInt(3, sale.getSoldBy());
            psSale.setDouble(4, sale.getTotalAmount());
            psSale.setString(5, sale.getPaymentMethod());
            psSale.setString(6, sale.getSaleStatus());
            psSale.executeUpdate();

            rsKeys = psSale.getGeneratedKeys();
            int saleId = -1;
            if (rsKeys.next()) {
                saleId = rsKeys.getInt(1);
                sale.setSaleId(saleId);
            } else {
                throw new SQLException("Failed to retrieve generated sale ID.");
            }

            // 3. Insert Items & deduct stock
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
            String deductSql = "UPDATE inventory SET quantity_in_stock = quantity_in_stock - ? WHERE product_id = ?";
            
            psItem = conn.prepareStatement(itemSql);
            psStockDeduct = conn.prepareStatement(deductSql);

            for (SaleItem item : items) {
                item.setSaleId(saleId);
                psItem.setInt(1, saleId);
                psItem.setInt(2, item.getProductId());
                psItem.setInt(3, item.getQuantity());
                psItem.setDouble(4, item.getUnitPrice());
                psItem.setDouble(5, item.getSubtotal());
                psItem.executeUpdate();

                psStockDeduct.setInt(1, item.getQuantity());
                psStockDeduct.setInt(2, item.getProductId());
                psStockDeduct.executeUpdate();
            }

            conn.commit(); // Success
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            if (e instanceof InsufficientStockException) {
                throw (InsufficientStockException) e;
            }
            throw new DatabaseException("Sales transaction failed: " + e.getMessage(), e);
        } finally {
            try { if (rsKeys != null) rsKeys.close(); } catch (SQLException e) {}
            try { if (rsStock != null) rsStock.close(); } catch (SQLException e) {}
            try { if (psSale != null) psSale.close(); } catch (SQLException e) {}
            try { if (psItem != null) psItem.close(); } catch (SQLException e) {}
            try { if (psStockCheck != null) psStockCheck.close(); } catch (SQLException e) {}
            try { if (psStockDeduct != null) psStockDeduct.close(); } catch (SQLException e) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {}
                DBConnection.releaseConnection(conn);
            }
        }
    }

    /**
     * Generates a unique invoice number.
     * Format: INV-yyyyMMdd-XXXX where XXXX is incremental.
     */
    public String generateNextInvoiceNo() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = CURRENT_DATE";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
            String dateStr = LocalDateTime.now().format(dtf);
            return String.format("INV-%s-%04d", dateStr, count + 1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to generate invoice number: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }
}
