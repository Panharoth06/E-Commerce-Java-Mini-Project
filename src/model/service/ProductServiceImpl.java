package model.service;

import mapper.ProductMapper;
import model.dto.ProductResponDto;
import model.repository.ProductRepositoryImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductServiceImpl implements ProductService {
private final ProductRepositoryImpl productRepository = new ProductRepositoryImpl();


    @Override
    public List<ProductResponDto> findProductbyname(String name) {
        List<ProductResponDto> productResponDtoList = new ArrayList<>();
        productRepository.findByName(name)
                .stream()
                .filter(p->p.getIsDeleted().equals(false)).forEach(product -> {
            productResponDtoList.add(ProductMapper.productToProductResponDto(product));

        });
        if (productResponDtoList.isEmpty()){
            System.out.println("product not found");
        }
        return productResponDtoList;
    }

    @Override
    public List<ProductResponDto> findProductbycategory(String category) throws SQLException {
        List<ProductResponDto> productResponDtoList = new ArrayList<>();
        productRepository.findByCategory(category).stream()
                .filter(p->p.getIsDeleted().equals(false))
                .forEach(product -> {productResponDtoList.add(ProductMapper.productToProductResponDto(product));});

        if (productResponDtoList.isEmpty()){
            System.out.println("product not found");
        }
        return productResponDtoList;
    }
}
