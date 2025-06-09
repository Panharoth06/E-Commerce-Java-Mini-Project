package model.repository;

import configuration.DbConnection;
import model.entities.Product;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository implements Repository <Product, Integer>{
    @Override
    public Product save(Product product) {
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
