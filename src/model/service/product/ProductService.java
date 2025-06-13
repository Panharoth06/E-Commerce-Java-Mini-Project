package model.service.product;

import model.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> findProductByName(String name);
    List<ProductResponseDto> findProductByCategory(String category);
}