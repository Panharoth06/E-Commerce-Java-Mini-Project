package model.repository;

import configuration.DbConnection;
import model.entities.Cart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartRepository {
    public Cart save(Cart cartItem) {
        String sql = "";
        Optional<Cart> existingCartItem = findByUserIdAndProductId(cartItem.getUserId(), cartItem.getProductId());
        if (existingCartItem.isPresent()) {
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
        return null;
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


    public Optional<Cart> findByUserIdAndProductId(Integer userId, Long productId) {
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

    public List<Cart> findByUserIdAndProductUUIDs(int userId, List<String> productUUIDs) {
        List<Cart> carts = new ArrayList<>();
        if (productUUIDs.isEmpty()) return carts;

        StringBuilder query = new StringBuilder("""
                    SELECT c.id, c.user_id, c.p_id, c.quantity, c.added_at,
                           p.p_uuid AS product_uuid, p.p_name AS product_name, p.category, p.price
                    FROM carts c
                    JOIN products p ON c.p_id = p.id
                    WHERE c.user_id = ? AND p.p_uuid IN (
                """);

        query.append("?,".repeat(productUUIDs.size()));
        query.setLength(query.length() - 1); // remove last comma
        query.append(")");

        try (Connection conn = DbConnection.getDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            stmt.setInt(1, userId);
            for (int i = 0; i < productUUIDs.size(); i++) {
                stmt.setString(i + 2, productUUIDs.get(i));
            }

            rsExecuteQuery(carts, stmt);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return carts;
    }

    public List<Cart> findByUserId(int userId) {
        List<Cart> carts = new ArrayList<>();
        String sql = """
                    SELECT c.id, c.user_id, c.p_id, c.quantity, c.added_at,
                           p.p_uuid AS product_uuid, p.p_name AS product_name, p.category, p.price
                    FROM carts c
                    JOIN products p ON c.p_id = p.id
                    WHERE c.user_id = ?
                """;

        try (Connection conn = DbConnection.getDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cart cart = Cart.builder().id(rs.getInt("id")).userId(rs.getInt("user_id")).productId(rs.getLong("p_id")).quantity(rs.getInt("quantity")).addedAt(rs.getTimestamp("added_at").toLocalDateTime()).productUUID(rs.getString("product_uuid")).productName(rs.getString("product_name")).category(rs.getString("category")).price(rs.getBigDecimal("price")).build();
                    carts.add(cart);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding cart by user ID: " + e.getMessage());
        }
        return carts;
    }

    private void rsExecuteQuery(List<Cart> carts, PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cart cart = Cart.builder().id(rs.getInt("id")).userId(rs.getInt("user_id")).productId(rs.getLong("p_id")).quantity(rs.getInt("quantity")).addedAt(rs.getTimestamp("added_at").toLocalDateTime()).productUUID(rs.getString("product_uuid")).productName(rs.getString("product_name")).category(rs.getString("category")).price(rs.getBigDecimal("price")).build();

                carts.add(cart);
            }
        }
    }

    private Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        return Cart.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .productId(rs.getLong("p_id"))
                .quantity(rs.getInt("quantity"))
                .addedAt(rs.getTimestamp("added_at").toLocalDateTime())
                .build();
    }
}
