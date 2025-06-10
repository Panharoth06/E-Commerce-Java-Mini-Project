package controller;


import model.dto.ProductResponDto;
import model.service.ProductServiceImpl;

import java.util.List;

public class ProductController {
    private final ProductServiceImpl productServiceImpl = new ProductServiceImpl();
    public List<ProductResponDto> getProductsByName(String name) {
        return productServiceImpl.findProductbyname(name);
    }
    public List<ProductResponDto> getProductsByCategory(String category) {
        return productServiceImpl.findProductbycategory(category);
    }
}