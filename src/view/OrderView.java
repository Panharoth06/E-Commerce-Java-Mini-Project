package view;

import controller.OrderController;
import model.dto.CartResponseDto;
import model.entities.User;
import model.repository.UserRepositoryImpl;
import model.service.cart.CartImpl;

import java.sql.SQLOutput;
import java.util.*;

public class OrderView {
    private final OrderController orderController = new OrderController();
    private final UserRepositoryImpl  userRepository = new UserRepositoryImpl();
    private final User user = userRepository.getLoggedInUser();
    private final CartImpl cartImpl = new CartImpl();

    public void placeOrder() {
        System.out.println(user);
//        user.setId(2); // test user
        try {
            // Show all items in cart with UUIDs
            List<CartResponseDto> carts = cartImpl.getAllProductsInCart(user.getId());
            carts.forEach(System.out::println);

            Scanner scanner = new Scanner(System.in);
            System.out.print("\n🛒 Enter product UUIDs to order (comma-separated): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("⚠️ No UUIDs entered. Cancelled.");
                return;
            }

            Map<String, Integer> uuidQuantityMap = new HashMap<>();
            List<String> uuids = Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (String uuid : uuids) {
                System.out.print("🔢 Enter quantity for UUID " + uuid + ": ");
                int qty = Integer.parseInt(scanner.nextLine().trim());
                uuidQuantityMap.put(uuid, qty);
            }

            // Call controller with map of UUIDs and quantities
            orderController.orderController(user, uuidQuantityMap);

        } catch (Exception e) {
            System.err.println("❌ Error placing order: " + e.getMessage());
        }
    }

}

