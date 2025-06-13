package model.serviceImpl;

import model.entities.Product; // Import the entity
import model.dto.ProductDto; // Import the DTO
import model.mapper.ProductMapper;
import model.repository.ProductRepository;
import model.service.ProductService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService { // CORRECTED implements clause

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.productMapper = new ProductMapper();
    }

    @Override // This method's return type matches ProductService
    public List<ProductDto> getAllProducts() {
        List<Product> products = productRepository.findAllProducts();
        return productMapper.toProductDtoList(products);
    }

    @Override // This method's return type matches ProductService
    public Map<String, List<ProductDto>> getAllProductsGroupedByCategory() {
        List<Product> products = productRepository.findAllProducts();
        List<ProductDto> productDtos = productMapper.toProductDtoList(products);
        return productDtos.stream()
                .collect(Collectors.groupingBy(ProductDto::getCategory));
    }
}