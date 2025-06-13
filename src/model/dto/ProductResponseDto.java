package model.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponseDto(
        String productName,
        String category,
        BigDecimal price,
        Integer quantity
)
{ }
