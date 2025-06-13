package model.service;

import model.dto.ProductDto;

import java.util.List;
import java.util.Map;

public interface ProductService {
    List<ProductDto> getAllProducts();
    Map<String, List<ProductDto>> getAllProductsGroupedByCategory();
}
