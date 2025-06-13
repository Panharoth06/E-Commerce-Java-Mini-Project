package controller;

import model.entities.User;
import model.service.OrderImpl;
import model.service.cart.CartImpl;

import java.util.List;
import java.util.Map;

public class OrderController {
    private final OrderImpl order =  new OrderImpl();

    public void orderController(User user, Map<String, Integer> uuidQuantityMap) {
        order.placeOrder(user, uuidQuantityMap);
    }

}
