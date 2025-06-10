package model.service;

import mapper.ProductMapper;
import model.dto.ProductResponseDto;
import model.repository.ProductRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class ProductServiceImpl implements ProductService {

    private final ProductRepositoryImpl productRepository = new ProductRepositoryImpl();

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
}
