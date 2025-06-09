package model.dto;

import lombok.Builder;

@Builder
public record ProductResponseDto(
        String productName,
        String category,
        Double price,
        Integer quantity
)
{ }
