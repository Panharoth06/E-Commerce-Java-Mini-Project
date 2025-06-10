package model.service;

import model.dto.ProductResponDto;

import java.sql.SQLException;
import java.util.List;

public interface ProductService {
    List<ProductResponDto> findProductbyname(String name);
    List<ProductResponDto> findProductbycategory(String category) throws SQLException;
}
