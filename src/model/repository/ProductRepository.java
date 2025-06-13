
package model.repository;

import configuration.DbConnection;
import model.entities.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Needed for UUID type

public class ProductRepository {

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

    // Find product by its UUID
    public Optional<Product> findByProductUuid(UUID productUuid) {
        // Ensure p_uuid column in products table is type UUID or TEXT/VARCHAR
        // If it's TEXT/VARCHAR, 'p_uuid = ?' is fine. If UUID type, '?::uuid' is sometimes needed depending on driver/DB version.
        String sql = "SELECT id, p_name, category, price, qty, is_deleted, p_uuid, created_at FROM products WHERE p_uuid = ? AND is_deleted = FALSE";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, productUuid); // Set UUID object directly
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            // Include UUID in error message for better debugging
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

    // Helper method to map a ResultSet row to a Product object
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        String uuidString = rs.getString("p_uuid");
        UUID productUuid = null;
        if (uuidString != null) {
            try {
                productUuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                // Log a warning if a UUID string is malformed in the DB
                System.err.println("Warning: Invalid UUID format in database for product ID " + rs.getLong("id") + ": " + uuidString);
                // productUuid will remain null
            }
        }

        return Product.builder()
                .id(rs.getLong("id"))
                .productName(rs.getString("p_name"))
                .category(rs.getString("category"))
                .price(rs.getDouble("price"))
                .quantity(rs.getInt("qty"))
                .isDeleted(rs.getBoolean("is_deleted"))
                .productUuid(String.valueOf(productUuid)) // CORRECTED: Assign UUID object directly
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }
}