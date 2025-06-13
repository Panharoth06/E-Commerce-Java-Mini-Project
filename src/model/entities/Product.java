package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.dto.ProductDto;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    Long id;
    String productName;
    String category;
    Double price;
    Integer quantity;
    Boolean isDeleted;
    String productUuid;
    Date createdAt;

}
