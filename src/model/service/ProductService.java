package model.service;

import model.dto.ProductResponseDto;
import model.entities.Product;

import java.util.List;

public interface ProductService {
    List<ProductResponseDto> findProductByName(String name);
    List<ProductResponseDto> findProductByCategory(String category);
}