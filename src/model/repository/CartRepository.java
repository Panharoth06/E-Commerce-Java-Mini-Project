package model.repository;

import configuration.DbConnection;
import model.entities.Cart;
import model.entities.Product;
import model.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    public Cart save(Cart cart, Product product, User user) {
        try (Connection connection = DbConnection.getDatabaseConnection()) {
            String sql = """
                    INSERT INTO carts (user_id, p_id, quantity, added_at)
                    VALUES (?, ?, ?, ?)
                    RETURNING id, added_at
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setLong(2, product.getId());
            preparedStatement.setInt(3, cart.getQuantity());
            preparedStatement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            preparedStatement.executeUpdate();

            // Use JOIN to fetch cart with product name
            String fetchSql = """
                    SELECT c.*, p.p_name, p.p_uuid AS product_uuid
                    FROM carts c
                    JOIN products p ON c.p_id = p.id
                    WHERE c.user_id = ? AND c.p_id = ?
                    ORDER BY c.id DESC
                    LIMIT 1;
                    """;
            PreparedStatement fetchStmt = connection.prepareStatement(fetchSql);
            fetchStmt.setInt(1, user.getId());
            fetchStmt.setLong(2, product.getId());
            ResultSet rs = fetchStmt.executeQuery();

            if (rs.next()) {
                cart.setId(rs.getInt("id"));
                cart.setProductName(rs.getString("p_name")); // In Cart class
                cart.setProductUUID(rs.getString("product_uuid")); // Add this to Cart class
                cart.setQuantity(rs.getInt("quantity"));
                cart.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return cart;
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
}
