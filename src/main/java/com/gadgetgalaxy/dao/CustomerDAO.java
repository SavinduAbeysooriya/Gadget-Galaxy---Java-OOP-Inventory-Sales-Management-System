package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Customer;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer operations.
 */
public class CustomerDAO implements DAO<Customer> {

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address")
        );
    }

    @Override
    public Customer findById(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM customers WHERE customer_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find customer: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    public Customer findByPhone(String phone) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM customers WHERE phone = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, phone);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find customer by phone: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Customer> findAll() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Customer> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM customers";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToCustomer(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list customers: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean insert(Customer entity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO customers (customer_name, phone, email, address) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getCustomerName());
            ps.setString(2, entity.getPhone());
            ps.setString(3, entity.getEmail());
            ps.setString(4, entity.getAddress());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) entity.setCustomerId(gk.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert customer: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Customer entity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE customers SET customer_name = ?, phone = ?, email = ?, address = ? WHERE customer_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, entity.getCustomerName());
            ps.setString(2, entity.getPhone());
            ps.setString(3, entity.getEmail());
            ps.setString(4, entity.getAddress());
            ps.setInt(5, entity.getCustomerId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update customer: " + e.getMessage(), e);
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
            String sql = "DELETE FROM customers WHERE customer_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete customer: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }
}
