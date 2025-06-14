package view;

import controller.CartController;
import controller.OrderController;
import model.dto.CartResponseDto;
import model.entities.User;
import model.repository.UserRepositoryImpl;

import java.util.*;

public class OrderView {
    private final OrderController orderController = new OrderController();
    private final UserRepositoryImpl  userRepository = new UserRepositoryImpl();
    private final CartController cartController = new CartController();
    private final User user = userRepository.getLoggedInUser();

    public void placeOrder() {
        try {
            List<CartResponseDto> carts = cartController.getAllProductInCart(user);
            if (carts.isEmpty()) {
                System.out.println("[!] There are no products in cart.");
                return;
            }
            else {
                new TableUI<CartResponseDto>().getTableDisplay(carts);
            }

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

            orderController.orderController(user, uuidQuantityMap);

        } catch (Exception e) {
            System.out.println("❌ Error placing order: " + e.getMessage());
        }
    }

}

