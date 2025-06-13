package controller;

import model.dto.ProductResponseDto;
import model.service.product.ProductServiceImpl;
import view.ProductView;

import java.util.List;
import java.util.Map;

public class ProductController {
    private final ProductServiceImpl productServiceImpl = new ProductServiceImpl();
    public List<ProductResponseDto> getProductByName(String name){
        return productServiceImpl.findProductByName(name);
    }
    public List<ProductResponseDto> getProductByCategory(String category){
        return productServiceImpl.findProductByCategory(category);
    }

    public List<ProductResponseDto> getAllProducts() {
        return productServiceImpl.getAllProducts();
    }

    public void listAllProductsInStore() {
        List<ProductResponseDto> allProducts = productServiceImpl.getAllProducts();
    }


    public Map<String, List<ProductResponseDto>> listAllProductsInStoreSeparatedByCategory() {
        return productServiceImpl.getAllProductsGroupedByCategory();
    }
}