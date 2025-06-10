package model.repository;

import configuration.DbConnection;
import model.entities.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository implements Repository <Product, Integer>{
    @Override
    public Product save(Product product) {

        try (Connection connection = DbConnection.getDatabaseConnection()) {
            String sql = """
                    INSERT INTO products (p_name, category, price, qty, is_deleted, p_uuid, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getProductName());
            statement.setString(2, product.getCategory());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());
            statement.setBoolean(5, product.getIsDeleted());
            statement.setString(6, product.getProductUuid());
            statement.setTimestamp(7, (Timestamp) product.getCreatedAt());
            int rowAffected = statement.executeUpdate();
            if (rowAffected > 0) return product;
        }catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<Product> findAll() {
        try (Connection conn = DbConnection.getDatabaseConnection()) {
            String query = "SELECT * FROM products";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            List<Product> products = new ArrayList<>();
            Product product;
            while (resultSet.next()) {
                product = new Product();
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Integer delete(Product product) {
        return 0;
    }
}
