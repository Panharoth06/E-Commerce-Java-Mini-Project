package model.service.product;

import model.dto.CreateProductDto;
import model.dto.ProductResponseDto;
import model.entities.Product;
import model.mapper.ProductMapper;
import model.repository.ProductRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductImpl  {
    private final ProductRepository productRepository = new ProductRepository();

//    @Override
//    public ProductResponseDto addProduct(CreateProductDto createProductDto) {
//        Product product = ProductMapper.fromCreateProductDtoToProduct(createProductDto);
//        product.setProductName(product.getProductName());
//        product.setProductUuid(UUID.randomUUID().toString());
//        product.setIsDeleted(false);
//        product.setCreatedAt(new Timestamp(System.currentTimeMillis()));
//        System.out.println(product);
//        return ProductMapper.fromProductToProductResponseDto(productRepository.save(product));
//    }

    public List<ProductResponseDto> findAllProducts() {
        List<ProductResponseDto> products = new ArrayList<>();
        new ProductRepository().findAll().forEach(product -> products.add(
                ProductMapper.fromProductToProductResponseDto(product)
        ));
        return products;
    }
}
