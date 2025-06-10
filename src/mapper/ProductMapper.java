package mapper;

import model.dto.ProductResponDto;
import model.entities.Product;

public class ProductMapper {
    public static ProductResponDto productToProductResponDto(Product product) {
        return ProductResponDto.builder().productName(product.getProductName()).category(product.getCategory())
                .price(String.valueOf(product.getPrice()))
                .quantity(product.getQuantity())
                .productName(product.getProductName())
                .build();
    }
}
