package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private Integer id;
    private Integer userId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime addedAt;
    private String productName;
    private String category;

    private int originalCartQuantity;
    private BigDecimal price;
    private String productUUID;

}
