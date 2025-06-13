package model.mapper;

import model.dto.CartResponseDto;
import model.entities.Cart;

public class CartMapper {
    public static CartResponseDto fromCartToCartResponseDto(Cart cart) {
        return CartResponseDto.builder()
                .productName(cart.getProductName())
                .category(cart.getCategory())
                .quantity(cart.getQuantity())
                .addedAt(cart.getAddedAt())
                .productUUID(cart.getProductUUID())
                .build();
    }
}
