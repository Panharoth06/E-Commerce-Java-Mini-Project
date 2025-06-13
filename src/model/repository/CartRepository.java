
package model.repository;

import configuration.DbConnection;
import model.entities.Cart;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartRepository {

    // Helper method to map a ResultSet row to a Cart object
    private Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        return Cart.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .productId(rs.getLong("p_id")) // CORRECTED: Use "p_id" to match DB
                .quantity(rs.getInt("quantity"))
                .added_at(rs.getTimestamp("added_at")) // Using addedAt to match Cart entity
                .build();
    }

    // Saves a new cart item or updates an existing one if it's already in the cart for that user
    public Cart save(Cart cartItem) {
        String sql;
        // Check if item already exists for the user and product
        // CORRECTED: Use p_id in findByUserIdAndProductId call
        Optional<Cart> existingCartItem = findByUserIdAndProductId(cartItem.getUserId(), cartItem.getProductId());

        if (existingCartItem.isPresent()) {
            // Update existing quantity
            // CORRECTED: Use p_id in WHERE clause
            sql = "UPDATE carts SET quantity = quantity + ?, added_at = NOW() WHERE user_id = ? AND p_id = ? RETURNING id, user_id, p_id, quantity, added_at";
            try (Connection conn = DbConnection.getDatabaseConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, cartItem.getQuantity());
                stmt.setInt(2, cartItem.getUserId());
                stmt.setLong(3, cartItem.getProductId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return mapResultSetToCart(rs);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error updating cart item: " + e.getMessage(), e);
            }
        } else {
            // Insert new item
            // CORRECTED: Use p_id in INSERT and RETURNING clauses
            sql = "INSERT INTO carts (user_id, p_id, quantity, added_at) VALUES (?, ?, ?, NOW()) RETURNING id, user_id, p_id, quantity, added_at";
            try (Connection conn = DbConnection.getDatabaseConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, cartItem.getUserId());
                stmt.setLong(2, cartItem.getProductId());
                stmt.setInt(3, cartItem.getQuantity());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return mapResultSetToCart(rs);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error inserting cart item: " + e.getMessage(), e);
            }
        }
        return null; // Indicate failure or return a more meaningful type
    }

    // Find all cart items for a specific user
    public List<Cart> findByUserId(Integer userId) {
        List<Cart> cartItems = new ArrayList<>();
        // Join with products table to get product details for CartDto mapping later
        // CORRECTED: Use c.p_id in SELECT and JOIN ON clause
        String sql = "SELECT c.id, c.user_id, c.p_id, c.quantity, c.added_at, " +
                "p.p_name, p.category, p.price " +
                "FROM carts c JOIN products p ON c.p_id = p.id " +
                "WHERE c.user_id = ? ORDER BY c.added_at ASC";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cartItems.add(mapResultSetToCart(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cart items for user ID " + userId + ": " + e.getMessage(), e);
        }
        return cartItems;
    }

    // Find a specific cart item by user ID and product ID
    public Optional<Cart> findByUserIdAndProductId(Integer userId, Long productId) {
        // CORRECTED: Use p_id in SELECT and WHERE clause
        String sql = "SELECT id, user_id, p_id, quantity, added_at FROM carts WHERE user_id = ? AND p_id = ?";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setLong(2, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCart(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cart item by user ID and product ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Update quantity of an existing cart item - No changes here as it uses 'id'
    public boolean updateQuantity(Integer cartItemId, Integer newQuantity) {
        String sql = "UPDATE carts SET quantity = ?, added_at = NOW() WHERE id = ?";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, cartItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating cart item quantity: " + e.getMessage(), e);
        }
    }

    // Remove a cart item - No changes here as it uses 'id'
    public boolean delete(Integer cartItemId) {
        String sql = "DELETE FROM carts WHERE id = ?";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting cart item: " + e.getMessage(), e);
        }
    }

    // Clear all items for a user's cart - No changes here as it uses 'user_id'
    public boolean clearCart(Integer userId) {
        String sql = "DELETE FROM carts WHERE user_id = ?";
        try (Connection conn = DbConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing cart for user " + userId + ": " + e.getMessage(), e);
        }
    }
}