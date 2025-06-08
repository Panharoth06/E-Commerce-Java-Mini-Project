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
public class Cart {
    Integer id;
    Integer userId;
    Long productId;
    Integer quantity;
    Date added_at;
}
