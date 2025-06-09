package model.dto;

import lombok.Builder;

@Builder
public record CreateProductDto (
        String productName,
        String category,
        Double price,
        Integer quantity
)
{ }
