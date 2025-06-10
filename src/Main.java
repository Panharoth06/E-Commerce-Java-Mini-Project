import configuration.DbConnection;
import controller.ProductController;
import model.dto.CreateProductDto;
import model.repository.ProductRepository;
import util.PasswordEncryptor;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
//        Connection connection = DbConnection.getDatabaseConnection();
//        String password = PasswordEncryptor.hashPassword("koko!@#$@!");
//        System.out.println(password);
//        boolean isMatch = PasswordEncryptor.checkPassword("koko!@#$@!", password);
//        if (isMatch) System.out.println("login successful");
//        else System.out.println("login failed");
        new ProductController().addProduct(new CreateProductDto("Bubble Tea", "Tea", 1.5, 30));
        new ProductController().getAllProducts().forEach(System.out::println);


    }
}
