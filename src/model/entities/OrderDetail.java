package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    Integer id;
    Integer orderId;
    Long productId;
    Integer quantity;
    Double EachPrice;
}
