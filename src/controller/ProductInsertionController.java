package controller;

import configuration.DbConnection;
import model.repository.ProductRepositoryInsertion;
//import model.service.ProductInsertion;

import java.sql.Connection;
import java.sql.SQLException;

// Entry point of the application, orchestrates the service layer
public class ProductInsertionController {

     public static void run() throws SQLException {
         try (Connection connection = DbConnection.getDatabaseConnection()) {
            ProductRepositoryInsertion productRepositoryInsertion = new ProductRepositoryInsertion(connection);
            productRepositoryInsertion.dropConstraintsAndIndexes();
            productRepositoryInsertion.insert10Million();
            productRepositoryInsertion.restoreConstraintsAndIndexes();
            productRepositoryInsertion.readAll();

         } catch (Exception e) {
             throw new RuntimeException(e);
         }
    }
}
