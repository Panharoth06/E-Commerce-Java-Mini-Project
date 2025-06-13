package model.mapper;

import model.dto.ProductResponseDto;
import model.entities.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {
    public static ProductResponseDto fromProductToProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .productName(product.getProductName())
                .category(product.getCategory())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .productUuid(product.getProductUuid())
                .build();
    }

    public ProductResponseDto toProductDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductResponseDto.builder()
                .productName(product.getProductName())
                .category(product.getCategory())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .productUuid(product.getProductUuid())
                .build();
    }
    public List<ProductResponseDto> toProductDtoList(List<Product> products) {
        if (products == null) {
            return List.of();
        }
        return products.stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }



}
