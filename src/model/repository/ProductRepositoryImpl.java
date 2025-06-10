package model.repository;

import configuration.DbConnection;
import model.entities.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImpl implements  Repository<Product,Integer> {

    @Override
    public Product save(Product product) {
        return null;
    }

    @Override
    public List<Product> findAll() {
        return List.of();
    }

    @Override
    public Integer delete(Integer id) {
        return 0;
    }

    public List<Product> findProductByName(String name) {
        try(Connection con = DbConnection.getDatabaseConnection()){
            String sql = """
                    SELECT * FROM products
                    WHERE p_name ILIKE ?
                    """;
            PreparedStatement pre = con.prepareStatement(sql);
            pre.setString(1, name+"%");
            ResultSet resultSet = pre.executeQuery();
            List<Product> products = new ArrayList<>();
            while(resultSet.next()){
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
        }catch(Exception e){
            System.out.println("[!] Product not found: " +e.getMessage());
        }
        return null;
    }
    public List<Product> findProductByCategory(String category) {
        try(Connection con = DbConnection.getDatabaseConnection()){
            String sql = """
                    SELECT * FROM products
                    WHERE category ILIKE ?
                    """;
            PreparedStatement pre = con.prepareStatement(sql);
            pre.setString(1, category+"%");
            ResultSet resultSet = pre.executeQuery();
            List<Product> products = new ArrayList<>();
            while(resultSet.next()){
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
        }catch(Exception e){
            System.out.println("[!] Product not found: " +e.getMessage());
        }
        return  null;
    }
}
