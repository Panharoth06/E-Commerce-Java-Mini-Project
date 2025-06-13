package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    Integer id;
    Integer orderId;
    Long productId;
    Integer quantity;
    BigDecimal EachPrice;
}
