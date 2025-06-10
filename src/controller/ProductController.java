package controller;

import model.dto.ProductResponseDto;
import model.service.ProductServiceImpl;

import java.util.List;

public class ProductController {
    private final ProductServiceImpl productServiceImpl = new ProductServiceImpl();
    public List<ProductResponseDto> getProductByName(String name){
        return productServiceImpl.findProductByName(name);
    }
    public List<ProductResponseDto> getProductByCategory(String category){
        return productServiceImpl.findProductByCategory(category);
    }
}
