package model.service.product;

import model.dto.ProductResponseDto;
import model.entities.Product;
import model.mapper.ProductMapper;
import model.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository = new ProductRepository();

    @Override
    public List<ProductResponseDto> findProductByName(String name) {
        List<ProductResponseDto> productResponseDtoList = new ArrayList<>();
        productRepository.findProductByName(name)
                .stream()
                .filter(p->p.getIsDeleted().equals(false))
                .forEach(product -> {
                    productResponseDtoList.add(ProductMapper.fromProductToProductResponseDto(product));
                });
        if(productResponseDtoList.isEmpty()){
            System.out.println("[!] Product not found");
        }
        return productResponseDtoList;
    }

    @Override
    public List<ProductResponseDto> findProductByCategory(String category) {
        List<ProductResponseDto> productResponseDtoList = new ArrayList<>();
        productRepository.findProductByCategory(category)
                .stream()
                .filter(p->p.getIsDeleted().equals(false))
                .forEach(product -> {
                    productResponseDtoList.add(ProductMapper.fromProductToProductResponseDto(product));
                });
        if(productResponseDtoList.isEmpty()){
            System.out.println("[!] Product not found");
        }
        return productResponseDtoList;
    }

    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAllProducts();
        return new ProductMapper().toProductDtoList(products);
    }

    public Map<String, List<ProductResponseDto>> getAllProductsGroupedByCategory() {
        List<ProductResponseDto> productResponseDto = new ProductMapper().toProductDtoList(productRepository.findAllProducts());
        return productResponseDto.stream()
                .collect(Collectors.groupingBy(ProductResponseDto::category));
    }
}