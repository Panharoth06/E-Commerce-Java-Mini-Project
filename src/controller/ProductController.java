package controller;

import model.dto.CreateProductDto;
import model.dto.ProductResponseDto;
import model.service.product.ProductImpl;
import model.service.product.ProductService;

import java.util.List;

public class ProductController {
    private final ProductService productService = new ProductImpl();
    public ProductResponseDto addProduct(CreateProductDto createProductDto) {
        return productService.addProduct(createProductDto);
    }

    public List<ProductResponseDto> getAllProducts() {
        return productService.findAllProducts();
    }
}
