package model.service.product;

import model.dto.CreateProductDto;
import model.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto addProduct(CreateProductDto createProductDto);
    List<ProductResponseDto> findAllProducts();
}
