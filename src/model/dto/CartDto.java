// src/model/dto/CartDto.java
package model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for displaying a single item in the shopping cart.
 * Includes relevant product details to avoid fetching them separately in the view.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private Integer cartItemId;  // ID of the specific cart entry
    private Long productId;      // ID of the product
    private String productName;  // Name of the product
    private String category;     // Category of the product
    private Double unitPrice;    // Price per unit of the product
    private Integer quantity;    // Quantity of this product in the cart
    private Double totalPrice;   // Total price for this line item (unitPrice * quantity)
}