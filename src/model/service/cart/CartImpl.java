package model.service.cart;

import model.dto.CartResponseDto;
import model.entities.Cart;
import model.entities.Product;
import model.entities.User;
import model.mapper.CartMapper;
import model.repository.CartRepository;
import model.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class CartImpl {
    private final CartRepository cartRepository = new CartRepository();
    private final ProductRepository productRepository = new ProductRepository();

    public List<CartResponseDto> getAllProductsInCart(int userId) {
        return cartRepository.findByUserId(userId).stream()
                .map(cart -> CartResponseDto
                        .builder()
                        .productName(cart.getProductName())
                        .category(cart.getCategory())
                        .price(cart.getPrice().doubleValue())
                        .quantity(cart.getQuantity())
                        .addedAt(cart.getAddedAt())
                        .productUUID(cart.getProductUUID())
                        .build())
                .toList();
    }

    public boolean updateCartItemQuantity(Integer userId, List<String> productUuid, Integer newQuantity) {

        if (userId == null || productUuid.isEmpty() || newQuantity == null || newQuantity < 0) {
            System.err.println("Invalid input for updating cart quantity.");
            return false;
        }

        Optional<Cart> existingCartItemOptional = cartRepository.findByUserIdAndProductUUIDs(userId, productUuid).stream().findFirst();
        if (existingCartItemOptional.isEmpty()) {
            System.out.println("Cart item not found for user " + userId + " and product " + productUuid);
            return false;
        }
        Cart existingCartItem = existingCartItemOptional.get();

//         Check product stock before updating
        Optional<Product> productOptional = productRepository.findById(existingCartItem.getProductId());
        if (productOptional.isEmpty()) {
            System.err.println("Product associated with cart item not found.");
            return false;
        }
        Product product = productOptional.get();
        if (product.getQuantity() < newQuantity) {
            System.err.println("Not enough stock for product " + product.getProductName() + ". Available: " + product.getQuantity() + ", requested: " + newQuantity);
            return false;
        }

        try {
            if (newQuantity == 0) {
                // If new quantity is 0, remove the item
                return cartRepository.delete(existingCartItem.getProductId());
            } else {
                return cartRepository.updateQuantity(existingCartItem.getId(), newQuantity);
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to update cart item quantity: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCartItemQuantity(Integer userId, String productUuid) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        cartRepository.findByUserId(userId).forEach(cart -> {
             atomicBoolean.set(cartRepository.deleteByUserIdAndProductUUID(userId,  productUuid));
         });
        return atomicBoolean.get();
    }

    public boolean clearUserCart(Integer userId) {
        if (userId == null) {
            System.out.println("Invalid user ID for clearing cart.");
            return false;
        }
        try {
            return cartRepository.clearCart(userId);
        } catch (RuntimeException e) {
            System.out.println("Failed to clear cart for user " + userId + ": " + e.getMessage());
            return false;
        }
    }
}


