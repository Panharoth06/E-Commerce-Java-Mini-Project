package model.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CartResponseDto(
        String productName,
        String category,
        Integer quantity,
        LocalDateTime addedAt,
        Double price,
        String productUUID
) { }
