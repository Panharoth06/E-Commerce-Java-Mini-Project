package model.mapper;

import model.dto.ProductDto;
import model.entities.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {
    public ProductDto toProductDto(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDto.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public List<ProductDto> toProductDtoList(List<Product> products) {
        if (products == null) {
            return List.of();
        }
        return products.stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }
}
