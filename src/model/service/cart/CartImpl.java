package model.service.cart;

import model.dto.CartResponseDto;
import model.repository.CartRepository;

import java.util.List;

public class CartImpl {
    private final CartRepository cartRepository = new CartRepository();

    public List<CartResponseDto> getAllProductsInCart(int userId) {
        return cartRepository.findByUserId(userId).stream()
                .map(cart -> CartResponseDto.builder()
                        .productName(cart.getProductName())
                        .category(cart.getCategory())
                        .price(cart.getPrice().doubleValue())
                        .quantity(cart.getQuantity())
                        .addedAt(cart.getAddedAt())
                        .productUUID(cart.getProductUUID())
                        .build())
                .toList();
    }
}


