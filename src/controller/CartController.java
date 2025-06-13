package controller;

import lombok.RequiredArgsConstructor;
import model.dto.CartResponseDto;
import model.entities.User;
import model.service.cart.CartImpl;

import java.util.List;

@RequiredArgsConstructor
public class CartController {
    private final CartImpl cart;

    List<CartResponseDto> productResponseDtoList(User user) {
        return cart.getAllProductsInCart(user.getId());
    }
}
