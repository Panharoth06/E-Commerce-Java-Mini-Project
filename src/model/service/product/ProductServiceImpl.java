package model.service.product;

import model.dto.ProductResponseDto;
import model.entities.Cart;
import model.entities.Product;
import model.mapper.ProductMapper;
import model.repository.CartRepository;
import model.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository = new ProductRepository();
    private final CartRepository cartRepository = new CartRepository();

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

    public boolean addProductToCart(Integer userId, String productUuid, Integer quantity) {
        if (userId == null || productUuid == null || quantity == null || quantity <= 0) {
            System.err.println("Invalid input for adding to cart.");
            return false;
        }

        Optional<Product> productOptional = productRepository.findByProductUuid(productUuid);
        if (productOptional.isEmpty()) {
            System.err.println("Product with UUID " + productUuid + " not found.");
            return false;
        }

        Product product = productOptional.get();

        // Check if product is in stock
        if (product.getQuantity() < quantity) {
            System.err.println("Not enough stock for product " + product.getProductName() + ". Available: " + product.getQuantity());
            return false;
        }

        Cart cartItem = Cart.builder()
                .userId(userId)
                .productId(product.getId())
                .quantity(quantity)
                .build();

        try {
            Cart savedCartItem = cartRepository.save(cartItem);
            return savedCartItem != null;
        } catch (RuntimeException e) {
            System.err.println("Failed to add/update product in cart: " + e.getMessage());
            return false;
        }
    }
}