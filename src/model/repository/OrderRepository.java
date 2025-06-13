package model.repository;

import configuration.DbConnection;
import model.entities.Cart;
import model.entities.Order;
import model.entities.OrderDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    public int createOrder(Connection conn, Order order) throws SQLException {
        String sql = """
                    INSERT INTO orders (user_id, order_code, total_price)
                    VALUES (?, ?, ?)
                    RETURNING id
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getUserId());
            stmt.setString(2, order.getOrderCode());
            stmt.setBigDecimal(3, order.getTotalPrice());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return -1;
    }

    public void insertOrderDetails(Connection conn, int orderId, List<OrderDetail> details) throws SQLException {
        String sql = """
                    INSERT INTO order_details (order_id, product_id, quantity, price_each)
                    VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderDetail d : details) {
                stmt.setInt(1, orderId);
                stmt.setLong(2, d.getProductId());
                stmt.setInt(3, d.getQuantity());
                stmt.setBigDecimal(4, d.getEachPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Cart> getCartByUser(Connection conn, Integer userId) throws SQLException {
        List<Cart> carts = new ArrayList<>();
        String sql = """
                    SELECT c.*, p.price FROM carts c
                    JOIN products p ON c.p_id = p.id
                    WHERE c.user_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Cart cart = Cart.builder().id(rs.getInt("id")).userId(rs.getInt("user_id")).productId(rs.getLong("p_id")).quantity(rs.getInt("quantity")).addedAt(rs.getTimestamp("added_at").toLocalDateTime()).build();
                cart.setPrice(rs.getBigDecimal("price")); // Add getter/setter for price
                carts.add(cart);
            }
        }
        return carts;
    }

    public void updateProductQuantities(Connection conn, List<Cart> carts) throws SQLException {
        String selectSql = "SELECT qty FROM products WHERE id = ?";
        String updateSql = "UPDATE products SET qty = qty - ? WHERE id = ?";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql); PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            for (Cart c : carts) {
                // Check available quantity
                selectStmt.setLong(1, c.getProductId());
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        int availableQty = rs.getInt("qty");
                        if (availableQty < c.getQuantity()) {
                            throw new SQLException("❌ Not enough stock for product ID: " + c.getProductId() + " (Requested: " + c.getQuantity() + ", Available: " + availableQty + ")");
                        }

                        // Add to batch if enough stock
                        updateStmt.setInt(1, c.getQuantity());
                        updateStmt.setLong(2, c.getProductId());
                        updateStmt.addBatch();
                    } else {
                        throw new SQLException("❌ Product not found for ID: " + c.getProductId());
                    }
                }
            }
            // Execute safe batch
            updateStmt.executeBatch();
        }
    }

    public void clearCart(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM carts WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public List<Cart> findByUserIdAndProductUUIDs(int userId, List<String> productUUIDs) {
        List<Cart> carts = new ArrayList<>();

        if (productUUIDs == null || productUUIDs.isEmpty()) return carts;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < productUUIDs.size(); i++) {
            placeholders.append("?");
            if (i < productUUIDs.size() - 1) placeholders.append(", ");
        }

        String sql = """
                    SELECT c.*, p.price
                    FROM carts c
                    JOIN products p ON c.p_id = p.id
                    WHERE c.user_id = ?
                    AND p.uuid IN (%s)
                """.formatted(placeholders.toString());

        try (Connection conn = DbConnection.getDatabaseConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            for (int i = 0; i < productUUIDs.size(); i++) {
                stmt.setString(i + 2, productUUIDs.get(i)); // +2 because userId is the first parameter
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Cart cart = Cart.builder().id(rs.getInt("id")).userId(rs.getInt("user_id")).productId(rs.getLong("p_id")).quantity(rs.getInt("quantity")).addedAt(rs.getTimestamp("added_at").toLocalDateTime()).build();
                cart.setPrice(rs.getBigDecimal("price"));
                carts.add(cart);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching cart by product UUIDs: " + e.getMessage());
        }

        return carts;
    }

    public void updateOrRemoveOrderedItems(Connection conn, int userId, List<Cart> orderedCarts) throws SQLException {
        String deleteSQL = "DELETE FROM carts WHERE user_id = ? AND p_id = ?";
        String updateSQL = "UPDATE carts SET quantity = quantity - ? WHERE user_id = ? AND p_id = ?";

        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL);
             PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {

            for (Cart cart : orderedCarts) {
                if (cart.getQuantity() == cart.getOriginalCartQuantity()) {
                    // Full quantity ordered → delete
                    deleteStmt.setInt(1, userId);
                    deleteStmt.setLong(2, cart.getProductId());
                    deleteStmt.addBatch();
                } else {
                    // Partial quantity ordered → update
                    int diff = cart.getOriginalCartQuantity() - cart.getQuantity();
                    updateStmt.setInt(1, diff); // subtract this amount
                    updateStmt.setInt(2, userId);
                    updateStmt.setLong(3, cart.getProductId());
                    updateStmt.addBatch();
                }
            }

            deleteStmt.executeBatch();
            updateStmt.executeBatch();
        }
    }

    public void reduceCartQuantities(Connection conn, int userId, List<Cart> carts) throws SQLException {
        String updateSql = "UPDATE carts SET quantity = quantity - ? WHERE user_id = ? AND p_id = ?";
        String deleteSql = "DELETE FROM carts WHERE user_id = ? AND p_id = ?";

        try (
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)
        ) {
            for (Cart c : carts) {
                int newQty = getCurrentCartQuantity(conn, userId, c.getProductId()) - c.getQuantity();
                if (newQty <= 0) {
                    // delete if quantity becomes 0 or less
                    deleteStmt.setInt(1, userId);
                    deleteStmt.setLong(2, c.getProductId());
                    deleteStmt.addBatch();
                } else {
                    // just reduce
                    updateStmt.setInt(1, c.getQuantity());
                    updateStmt.setInt(2, userId);
                    updateStmt.setLong(3, c.getProductId());
                    updateStmt.addBatch();
                }
            }
            updateStmt.executeBatch();
            deleteStmt.executeBatch();
        }
    }

    private int getCurrentCartQuantity(Connection conn, int userId, long productId) throws SQLException {
        String sql = "SELECT quantity FROM carts WHERE user_id = ? AND p_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setLong(2, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }
        return 0;
    }




}
