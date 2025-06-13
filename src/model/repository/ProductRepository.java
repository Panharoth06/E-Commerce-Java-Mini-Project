package model.repository;

import configuration.DbConnection;
import model.entities.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductRepository {

    public List<Product> findProductByName(String name) {
        try (Connection con = DbConnection.getDatabaseConnection()) {
            String sql = """
                    SELECT * FROM products
                    WHERE p_name ILIKE ?
                    """;
            PreparedStatement pre = con.prepareStatement(sql);
            pre.setString(1, name + "%");
            ResultSet resultSet = pre.executeQuery();
            List<Product> products = new ArrayList<>();
            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getLong("id"));
                product.setProductName(resultSet.getString("p_name"));
                product.setCategory(resultSet.getString("category"));
                product.setPrice(resultSet.getDouble("price"));
                product.setQuantity(resultSet.getInt("qty"));
                product.setIsDeleted(resultSet.getBoolean("is_deleted"));
                product.setProductUuid(resultSet.getString("p_uuid"));
                product.setCreatedAt(resultSet.getTimestamp("created_at"));
                products.add(product);
            }
            return products;
        } catch (Exception e) {
            System.out.println("[!] Product not found: " + e.getMessage());
        }
        return null;
    }

    public List<Product> findProductByCategory(String category) {
        try (Connection con = DbConnection.getDatabaseConnection()) {
            String sql = """
                    SELECT * FROM products
                    WHERE category ILIKE ?
                    """;
            PreparedStatement pre = con.prepareStatement(sql);
            pre.setString(1, category + "%");
            ResultSet resultSet = pre.executeQuery();
            List<Product> products = new ArrayList<>();
            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getLong("id"));
                product.setProductName(resultSet.getString("p_name"));
                product.setCategory(resultSet.getString("category"));
                product.setPrice(resultSet.getDouble("price"));
                product.setQuantity(resultSet.getInt("qty"));
                product.setIsDeleted(resultSet.getBoolean("is_deleted"));
                product.setProductUuid(resultSet.getString("p_uuid"));
                product.setCreatedAt(resultSet.getTimestamp("created_at"));
                products.add(product);
            }
            return products;
        } catch (Exception e) {
            System.out.println("[!] Product not found: " + e.getMessage());
        }
        return null;
    }

    public Optional<Product> findById(Long id) {
        String sql = "SELECT id, p_name, category, price, qty, is_deleted, p_uuid, created_at FROM products WHERE id = ? AND is_deleted = FALSE";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Product> findByProductUuid(String productUuid) {
        String sql = "SELECT id, p_name, category, price, qty, is_deleted, p_uuid, created_at FROM products WHERE p_uuid = ? AND is_deleted = FALSE";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, productUuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by UUID '" + productUuid + "': " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Product> findAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, p_name, category, price, qty, is_deleted, p_uuid, created_at FROM products WHERE is_deleted = FALSE ORDER BY category, p_name";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all products: " + e.getMessage(), e);
        }
        return products;
    }
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        String uuidString = rs.getString("p_uuid");
//        System.out.println("UUID: " + uuidString); // debug
        if (uuidString != null) {
            try {
                UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid UUID in DB: " + uuidString);
            }
        }

        return Product.builder()
                .id(rs.getLong("id"))
                .productName(rs.getString("p_name"))
                .category(rs.getString("category"))
                .price(rs.getDouble("price"))
                .quantity(rs.getInt("qty"))
                .isDeleted(rs.getBoolean("is_deleted"))
                .productUuid(rs.getString("p_uuid"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }

}
