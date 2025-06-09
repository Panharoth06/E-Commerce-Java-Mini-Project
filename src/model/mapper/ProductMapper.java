package model.mapper;

import model.dto.ProductResponseDto;
import model.entities.Product;

public class ProductMapper {
    public static ProductResponseDto fromProductToProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .productName(product.getProductName())
                .category(product.getCategory())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}
