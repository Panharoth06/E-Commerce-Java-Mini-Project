// src/model/mapper/CartMapper.java
package model.mapper;

import model.dto.CartDto;
import model.entities.Cart;
import model.entities.Product; // We need product details for CartDto

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartMapper {

    // Converts a Cart entity and its corresponding Product entity to a CartDto
    public CartDto toCartDto(Cart cartItem, Product product) {
        if (cartItem == null || product == null) {
            return null;
        }
        return CartDto.builder()
                .cartItemId(cartItem.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .unitPrice(product.getPrice())
                .quantity(cartItem.getQuantity())
                .totalPrice(product.getPrice() * cartItem.getQuantity())
                .build();
    }
    public List<CartDto> toCartDtoList(List<Cart> cartItems, List<Product> productsInCart) {
        // Create a map of product ID to product for easy lookup
        Map<Long, Product> productMap = productsInCart.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        return cartItems.stream()
                .map(cartItem -> {
                    Product product = productMap.get(cartItem.getProductId());
                    return toCartDto(cartItem, product);
                })
                .collect(Collectors.toList());
    }
}