package model.serviceImpl;

import model.dto.CartDto;
import model.entities.Cart;
import model.entities.Product;
import model.mapper.CartMapper;
import model.repository.CartRepository;
import model.repository.ProductRepository;
import model.service.CartService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartMapper = new CartMapper();
    }

    @Override
    public boolean addProductToCart(Integer userId, UUID productUuid, Integer quantity) {
        if (userId == null || productUuid == null || quantity == null || quantity <= 0) {
            System.err.println("Invalid input for adding to cart.");
            return false;
        }

        Optional<Product> productOptional = productRepository.findByProductUuid(productUuid);
        if (productOptional.isEmpty()) {
            System.err.println("Product with UUID " + productUuid + " not found.");
            return false;
        }

        Product product = productOptional.get();

        // Check if product is in stock
        if (product.getQuantity() < quantity) {
            System.err.println("Not enough stock for product " + product.getProductName() + ". Available: " + product.getQuantity());
            return false;
        }

        Cart cartItem = Cart.builder()
                .userId(userId)
                .productId(product.getId())
                .quantity(quantity)
                .build();

        try {
            Cart savedCartItem = cartRepository.save(cartItem);
            return savedCartItem != null;
        } catch (RuntimeException e) {
            System.err.println("Failed to add/update product in cart: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<CartDto> viewCart(Integer userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            return List.of();
        }

        // Collect all unique product IDs from the cart items
        List<Long> productIds = cartItems.stream()
                .map(Cart::getProductId)
                .distinct()
                .collect(Collectors.toList());


        List<Product> productsInCart = productIds.stream()
                .map(productRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return cartMapper.toCartDtoList(cartItems, productsInCart);
    }

    @Override
    public boolean updateCartItemQuantity(Integer userId, Long productId, Integer newQuantity) {
        if (userId == null || productId == null || newQuantity == null || newQuantity <= 0) {
            System.err.println("Invalid input for updating cart quantity.");
            return false;
        }

        Optional<Cart> existingCartItemOptional = cartRepository.findByUserIdAndProductId(userId, productId);
        if (existingCartItemOptional.isEmpty()) {
            System.err.println("Cart item not found for user " + userId + " and product " + productId);
            return false;
        }
        Cart existingCartItem = existingCartItemOptional.get();

        // Check product stock before updating
        Optional<Product> productOptional = productRepository.findById(productId);
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
                return cartRepository.delete(existingCartItem.getId());
            } else {
                return cartRepository.updateQuantity(existingCartItem.getId(), newQuantity);
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to update cart item quantity: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeProductFromCart(Integer userId, Long productId) {
        if (userId == null || productId == null) {
            System.err.println("Invalid input for removing from cart.");
            return false;
        }
        Optional<Cart> existingCartItemOptional = cartRepository.findByUserIdAndProductId(userId, productId);
        if (existingCartItemOptional.isEmpty()) {
            System.err.println("Cart item not found for user " + userId + " and product " + productId);
            return false;
        }
        try {
            return cartRepository.delete(existingCartItemOptional.get().getId());
        } catch (RuntimeException e) {
            System.err.println("Failed to remove product from cart: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean clearUserCart(Integer userId) {
        if (userId == null) {
            System.err.println("Invalid user ID for clearing cart.");
            return false;
        }
        try {
            return cartRepository.clearCart(userId);
        } catch (RuntimeException e) {
            System.err.println("Failed to clear cart for user " + userId + ": " + e.getMessage());
            return false;
        }
    }
}