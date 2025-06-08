package model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    Integer id;
    Integer userId;
    String orderCode;
    Date orderDate;
    Double totalPrice;
}
