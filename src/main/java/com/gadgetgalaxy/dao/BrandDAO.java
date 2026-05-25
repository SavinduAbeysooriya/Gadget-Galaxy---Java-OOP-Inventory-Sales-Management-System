package com.gadgetgalaxy.dao;

import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Brand;
import com.gadgetgalaxy.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Brand operations.
 */
public class BrandDAO implements DAO<Brand> {

    private Brand mapResultSetToBrand(ResultSet rs) throws SQLException {
        return new Brand(
                rs.getInt("brand_id"),
                rs.getString("brand_name"),
                rs.getString("country")
        );
    }

    @Override
    public Brand findById(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM brands WHERE brand_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToBrand(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find brand: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public List<Brand> findAll() throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Brand> list = new ArrayList<>();
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM brands ORDER BY brand_name";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToBrand(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list brands: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean insert(Brand entity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO brands (brand_name, country) VALUES (?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getBrandName());
            ps.setString(2, entity.getCountry());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) entity.setBrandId(gk.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert brand: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Brand entity) throws DatabaseException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE brands SET brand_name = ?, country = ? WHERE brand_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, entity.getBrandName());
            ps.setString(2, entity.getCountry());
            ps.setInt(3, entity.getBrandId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update brand: " + e.getMessage(), e);
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
            String sql = "DELETE FROM brands WHERE brand_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete brand: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) {}
            DBConnection.releaseConnection(conn);
        }
    }
}
