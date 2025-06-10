package model.dto;

import lombok.Builder;

@Builder
public class ProductResponDto {
    String productName;
    String category;
    String price;
    Integer quantity;
    String ProductUuid;
}
