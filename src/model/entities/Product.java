package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Timestamp;
import java.util.Date;

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
