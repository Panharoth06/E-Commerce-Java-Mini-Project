package view;

import controller.ProductController;
import model.dto.ProductResponDto;
import model.entities.Product;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ProductView {
    private final static ProductController productController = new ProductController();
    private static void thumbnail() {
        System.out.println("""
                1.Search Product by Name
                2.Search Product by Category
                3.Exist
                """);
    }
    public static void home() {
        while (true) {
            thumbnail();
            System.out.println("Insert option");

            switch (new Scanner(System.in).nextInt()){
                case 1->{
                    System.out.println("Enter product name");
                    List<ProductResponDto> productResponDtoList = productController.getProductsByName(new Scanner(System.in).next());
                    Collections.reverse(productResponDtoList);
                    productResponDtoList.forEach(System.out::println);
                }
                case 2->{
                    System.out.println("Enter product category");
                    List<ProductResponDto> productResponDtoList = productController.getProductsByCategory(new Scanner(System.in).next());
                    Collections.reverse(productResponDtoList);
                    productResponDtoList.forEach(System.out::println);
                }
                case 3->{
                    System.out.println("Enter existing product");
                }
            }
        }
    }
};
