package model.service;

import configuration.DbConnection;
import model.entities.Cart;
import model.entities.Order;
import model.entities.OrderDetail;
import model.entities.User;
import model.repository.CartRepository;
import model.repository.OrderRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderImpl {
    private final OrderRepository orderRepo = new OrderRepository();
    private final CartRepository cartRepo = new CartRepository();

    public void placeOrder(User user, Map<String, Integer> uuidQuantityMap) {
        if (uuidQuantityMap == null || uuidQuantityMap.isEmpty()) {
            System.out.println("⚠️ No UUIDs or quantities provided.");
            return;
        }
        List<String> uuids = new ArrayList<>(uuidQuantityMap.keySet());
        List<Cart> carts = cartRepo.findByUserIdAndProductUUIDs(user.getId(), uuids);

        for (Cart c : carts) {
            int requestedQty = uuidQuantityMap.getOrDefault(c.getProductUUID(), 0);
            if (requestedQty <= 0) {
                throw new IllegalArgumentException("Invalid quantity for product: " + c.getProductName());
            }
            if (requestedQty > c.getQuantity()) {
                throw new IllegalArgumentException("❌ Requested quantity (" + requestedQty + ") exceeds what's in cart (" + c.getQuantity() + ") for: " + c.getProductName());
            }
            c.setOriginalCartQuantity(c.getQuantity());
            c.setQuantity(requestedQty);
        }


        if (carts.isEmpty()) {
            System.out.println("❌ No products found in your cart for the given UUIDs.");
            return;
        }

        try (Connection conn = DbConnection.getDatabaseConnection()) {
            conn.setAutoCommit(false);

            // order code
            String orderCode = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

            BigDecimal total = BigDecimal.ZERO;
            List<OrderDetail> details = new ArrayList<>();
            for (Cart cart : carts) {
                BigDecimal priceEach = cart.getPrice();
                BigDecimal lineTotal = priceEach.multiply(BigDecimal.valueOf(cart.getQuantity()));
                total = total.add(lineTotal);

                OrderDetail detail = new OrderDetail();
                detail.setProductId(cart.getProductId());
                detail.setQuantity(cart.getQuantity());
                detail.setEachPrice(priceEach);
                details.add(detail);
            }

            Order order = new Order();
            order.setUserId(user.getId());
            order.setOrderCode(orderCode);
            order.setTotalPrice(total);

            int orderId = orderRepo.createOrder(conn, order);
            order.setId(orderId);

            orderRepo.insertOrderDetails(conn, orderId, details);

            orderRepo.updateProductQuantities(conn, carts);

            orderRepo.reduceCartQuantities(conn, user.getId(), carts);

            conn.commit();

            System.out.println("✅ Order Placed Successfully");
            System.out.println("🧾 Order Code: " + order.getOrderCode());
            System.out.println("🛒 Products:");
            for (Cart cart : carts) {
                System.out.println("  - Product Name: "  + cart.getProductName() + " | Price: " + cart.getPrice() +  " | Quantity: " + cart.getQuantity());
            }
            System.out.println("💵 Total Price: $" + total);

        } catch (SQLException e) {
            System.err.println("❌ Order failed: " + e.getMessage());
        }
    }

}
