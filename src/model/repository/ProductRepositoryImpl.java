package model.repository;

import configuration.DbConnection;
import model.entities.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepositoryImpl implements Repository <Product,Integer>{
    @Override
    public Product save(Product product) {
        return null;
    }

    @Override
    public List<Product> findAll() {
        return List.of();
    }

    @Override
    public Integer findById(Integer id) {
        return 0;
    }

    @Override
    public Integer delete(Integer id) {
        return 0;
    }
    public List<Product> findByName(String name) {
        try(Connection con = DbConnection.getDatabaseConnection()){
            String sql = """
                                SELECT *
                                FROM product
                                WHERE name LIKE ?
                                """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1,"%"+name+"%");
            ResultSet rs = ps.executeQuery();
            List<Product> products = new ArrayList<>();

            while(rs.next()){
                Product product = new Product();
                product.setId(rs.getLong("id"));
                product.setProductName(rs.getString("product_name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantity(rs.getInt("quantity"));
                product.setIsDeleted(rs.getBoolean("is_deleted"));
                product.setProductUuid(rs.getString("product_uuid"));
                product.setCreatedAt(rs.getTimestamp("created_at"));
            }

        }catch(Exception e){
            System.out.println("product not found"+e.getMessage());
        }
        return null;
    }
    public List<Product> findByCategory(String category) throws SQLException {
        try(Connection con = DbConnection.getDatabaseConnection()){
            String sql = """
                    SELECT *
                    FROM product
                    WHERE category ILIKE ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1,category);
            ResultSet rs = ps.executeQuery();
            List<Product> products = new ArrayList<>();
            while(rs.next()){
                Product product = new Product();
                product.setId(rs.getLong("id"));
                product.setProductName(rs.getString("product_name"));
                product.setCategory(rs.getString("category"));
                product.setPrice(rs.getDouble("price"));
                product.setQuantity(rs.getInt("quantity"));
                product.setIsDeleted(rs.getBoolean("is_deleted"));
                product.setProductUuid(rs.getString("product_uuid"));
                product.setCreatedAt(rs.getTimestamp("created_at"));
                products.add(product);
            }
            return products;

        }catch (Exception e){
            System.out.println("product not found"+e.getMessage());
        }

        return null;
    }
}
