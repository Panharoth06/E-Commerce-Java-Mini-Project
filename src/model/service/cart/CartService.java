package model.service.cart;

import model.dto.CartResponseDto;
import model.dto.ProductResponseDto;

import java.util.List;

public interface CartService {
    List<ProductResponseDto> getAllProductsInCart ();
    ProductResponseDto getProductByName (String productName);
}
