// src/model/service/CartService.java
package model.service;

import model.dto.CartDto;

import java.util.List;
import java.util.UUID;

public interface CartService {

    boolean addProductToCart(Integer userId, UUID productUuid, Integer quantity);
    List<CartDto> viewCart(Integer userId);
    boolean updateCartItemQuantity(Integer userId, Long productId, Integer newQuantity);
    boolean removeProductFromCart(Integer userId, Long productId);
    boolean clearUserCart(Integer userId);
}