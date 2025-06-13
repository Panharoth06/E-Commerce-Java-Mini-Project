package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Integer id;
    private Integer userId;
    private String orderCode;
    private Date orderDate;
    private BigDecimal totalPrice;
}
