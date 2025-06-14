package controller;

import lombok.RequiredArgsConstructor;
import model.dto.CartResponseDto;
import model.entities.User;
import model.service.cart.CartImpl;
import model.service.product.ProductServiceImpl;

import java.util.List;

public class CartController {
    private final CartImpl cart = new CartImpl();

    public List<CartResponseDto> getAllProductInCart(User user) {
        return cart.getAllProductsInCart(user.getId());
    }

    public boolean addProductToCart(User user, String productUuid, int quantity) {
        return new ProductServiceImpl().addProductToCart(user.getId(), productUuid, quantity);
    }

    public boolean updateProductInCart(User user, String productUuid, int quantity) {
        return cart.updateCartItemQuantity(user.getId(), productUuid.lines().toList(), quantity);
    }

    public boolean removeProductFromCart(User user, String productUuid) {
        return cart.deleteCartItemQuantity(user.getId(), productUuid);
    }

    public boolean clearCart(User user) {
        return cart.clearUserCart(user.getId());
    }
}
