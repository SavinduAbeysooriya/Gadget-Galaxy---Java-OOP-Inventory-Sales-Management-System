package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.*;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product database operations.
 * Demonstrates PreparedStatement, Exception handling, Inheritance instantiation, and JDBC CRUD operations.
 */
public class ProductDAO implements DAO<Product> {

    // Specifications serialization helpers
    public static String serializeSpecs(Product product) {
        if (product instanceof Smartphone) {
            Smartphone sp = (Smartphone) product;
            return "OS: " + sp.getOsType() + ", RAM: " + sp.getRamSize() + "GB, Storage: " + sp.getStorageSize() + "GB";
        } else if (product instanceof Laptop) {
            Laptop lt = (Laptop) product;
            return "Processor: " + lt.getProcessorType() + ", RAM: " + lt.getRamSize() + "GB, Storage: " + lt.getStorageSize() + "GB, Screen: " + lt.getScreenSize() + "\"";
        } else if (product instanceof Tablet) {
            Tablet tb = (Tablet) product;
            return "OS: " + tb.getOsType() + ", Stylus: " + (tb.isHasStylusSupport() ? "Yes" : "No") + ", Screen: " + tb.getScreenSize() + "\"";
        } else if (product instanceof Accessory) {
            Accessory ac = (Accessory) product;
            return "Type: " + ac.getAccessoryType() + ", Wireless: " + (ac.isWireless() ? "Yes" : "No");
        }
        return product.getSpecifications();
    }

    private static String getSpecValue(String specs, String key) {
        if (specs == null) return "";
        int index = specs.indexOf(key);
        if (index == -1) return "";
        int start = index + key.length();
        int end = specs.indexOf(",", start);
        if (end == -1) {
            end = specs.length();
        }
        return specs.substring(start, end).trim();
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("product_id");
        String code = rs.getString("product_code");
        String name = rs.getString("product_name");
        String model = rs.getString("model");
        int categoryId = rs.getInt("category_id");
        int brandId = rs.getInt("brand_id");
        String specs = rs.getString("specifications");
        double unitPrice = rs.getDouble("unit_price");
        int warranty = rs.getInt("warranty_months");
        String imagePath = rs.getString("image_path");
        int createdBy = rs.getInt("created_by");
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        java.time.LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : null;

        // Instantiate subclasses based on categoryId
        if (categoryId == 1) { // Smartphones
            String os = getSpecValue(specs, "OS:");
            int ram = 0, storage = 0;
            try {
                ram = Integer.parseInt(getSpecValue(specs, "RAM:").replace("GB", ""));
                storage = Integer.parseInt(getSpecValue(specs, "Storage:").replace("GB", ""));
            } catch (Exception e) { /* fallback default */ }
            return new Smartphone(id, code, name, model, categoryId, brandId, specs, unitPrice, warranty, imagePath, createdBy, createdAt, os, ram, storage);
        } else if (categoryId == 2) { // Laptops
            String processor = getSpecValue(specs, "Processor:");
            int ram = 0, storage = 0;
            double screen = 0.0;
            try {
                ram = Integer.parseInt(getSpecValue(specs, "RAM:").replace("GB", ""));
                storage = Integer.parseInt(getSpecValue(specs, "Storage:").replace("GB", ""));
                screen = Double.parseDouble(getSpecValue(specs, "Screen:").replace("\"", ""));
            } catch (Exception e) { /* fallback default */ }
            return new Laptop(id, code, name, model, categoryId, brandId, specs, unitPrice, warranty, imagePath, createdBy, createdAt, processor, ram, storage, screen);
        } else if (categoryId == 3) { // Tablets
            String os = getSpecValue(specs, "OS:");
            boolean stylus = getSpecValue(specs, "Stylus:").equalsIgnoreCase("Yes");
            double screen = 0.0;
            try {
                screen = Double.parseDouble(getSpecValue(specs, "Screen:").replace("\"", ""));
            } catch (Exception e) { /* fallback default */ }
            return new Tablet(id, code, name, model, categoryId, brandId, specs, unitPrice, warranty, imagePath, createdBy, createdAt, os, stylus, screen);
        } else { // Category 4 (Smartwatches), 5 (Headphones), 6 (Accessories) -> Accessory
            String type = getSpecValue(specs, "Type:");
            if (type.isEmpty()) {
                if (categoryId == 4) type = "Smartwatch";
                else if (categoryId == 5) type = "Headphones";
                else type = "General";
            }
            boolean wireless = getSpecValue(specs, "Wireless:").equalsIgnoreCase("Yes");
            return new Accessory(id, code, name, model, categoryId, brandId, specs, unitPrice, warranty, imagePath, createdBy, createdAt, type, wireless);
        }
    }

    @Override
    public Product findById(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM products WHERE product_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find product by ID: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    public Product findByCode(String code) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM products WHERE product_code = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find product by code: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Product> findAll() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Product> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM products";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list products: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean insert(Product p) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO products (product_code, product_name, model, category_id, brand_id, specifications, unit_price, warranty_months, image_path, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getProductCode());
            ps.setString(2, p.getProductName());
            ps.setString(3, p.getModel());
            ps.setInt(4, p.getCategoryId());
            ps.setInt(5, p.getBrandId());
            
            // Auto serialize specifications
            String specs = serializeSpecs(p);
            p.setSpecifications(specs);
            ps.setString(6, specs);
            
            ps.setDouble(7, p.getUnitPrice());
            ps.setInt(8, p.getWarrantyMonths());
            ps.setString(9, p.getImagePath());
            ps.setInt(10, p.getCreatedBy());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        p.setProductId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert product: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Product p) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE products SET product_code = ?, product_name = ?, model = ?, category_id = ?, brand_id = ?, specifications = ?, unit_price = ?, warranty_months = ?, image_path = ? WHERE product_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, p.getProductCode());
            ps.setString(2, p.getProductName());
            ps.setString(3, p.getModel());
            ps.setInt(4, p.getCategoryId());
            ps.setInt(5, p.getBrandId());
            
            String specs = serializeSpecs(p);
            p.setSpecifications(specs);
            ps.setString(6, specs);
            
            ps.setDouble(7, p.getUnitPrice());
            ps.setInt(8, p.getWarrantyMonths());
            ps.setString(9, p.getImagePath());
            ps.setInt(10, p.getProductId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update product: " + e.getMessage(), e);
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
            String sql = "DELETE FROM products WHERE product_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete product: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }

    public List<Product> search(String query) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Product> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM products WHERE product_code LIKE ? OR product_name LIKE ? OR model LIKE ? OR specifications LIKE ?";
            ps = conn.prepareStatement(sql);
            String searchPattern = "%" + query + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search products: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignore */ }
            try { if (ps != null) ps.close(); } catch (SQLException e) { /* ignore */ }
            DBConnection.releaseConnection(conn);
        }
    }
}
