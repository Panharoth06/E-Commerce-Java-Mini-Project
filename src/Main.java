import configuration.DbConnection;
import controller.ProductController;
import model.dto.CreateProductDto;
import model.entities.User;
import model.repository.ProductRepository;
import model.service.OrderImpl;
import util.PasswordEncryptor;
import view.OrderView;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        Connection connection = DbConnection.getDatabaseConnection();
//        String password = PasswordEncryptor.hashPassword("koko!@#$@!");
//        System.out.println(password);
//        boolean isMatch = PasswordEncryptor.checkPassword("koko!@#$@!", password);
//        if (isMatch) System.out.println("login successful");
//        else System.out.println("login failed");
//        new ProductController().addProduct(new CreateProductDto("Bubble Tea", "Tea", 1.5, 30));
//        new ProductController().getAllProducts().forEach(System.out::println);
//        User user = new User();
//        user.setId(1);
//        OrderImpl order = new OrderImpl();
//        order.placeOrder(user, new ArrayList<>(
//                List.of("be966f31-37b7-49c0-8ebe-d4e483cf6f25")));
//
        OrderView orderView = new OrderView();
        orderView.placeOrder();
    }
}
