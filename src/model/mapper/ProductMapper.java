package model.mapper;

import model.dto.CreateProductDto;
import model.dto.ProductResponseDto;
import model.entities.Product;

import java.math.BigDecimal;

public class ProductMapper {
    public static ProductResponseDto fromProductToProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .productName(product.getProductName())
                .category(product.getCategory())
                .price(BigDecimal.valueOf(product.getPrice()))
                .quantity(product.getQuantity())
                .build();
    }
    public static Product fromCreateProductDtoToProduct(CreateProductDto createProductDto) {
        return Product
                .builder()
                .productName(createProductDto.productName())
                .category(createProductDto.category())
                .price(createProductDto.price())
                .quantity(createProductDto.quantity())
                .build();
    }
}
