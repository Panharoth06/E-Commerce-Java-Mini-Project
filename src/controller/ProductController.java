package controller;

import model.dto.ProductDto;
import model.service.ProductService;
import view.ProductView;

import java.util.List;
import java.util.Map;

public class ProductController {

    private final ProductService productService;
    private final ProductView productView;


    public ProductController(ProductService productService, ProductView productView) {
        this.productService = productService;
        this.productView = productView;
    }

    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }
    public void listAllProductsInStore() {
        List<ProductDto> allProducts = productService.getAllProducts();
        if (allProducts.isEmpty()) {
            productView.displayNoProductsFound();
        } else {
            productView.displayAllProducts(allProducts);
        }
    }


    public void listAllProductsInStoreSeparatedByCategory() {

        Map<String, List<ProductDto>> productsByCategory = productService.getAllProductsGroupedByCategory(); // Get DTOs

        if (productsByCategory.isEmpty()) {
            productView.displayNoProductsFound();
        } else {
            productView.displayProductsByCategory(productsByCategory);
        }
    }
}