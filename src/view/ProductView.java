package view;

import controller.ProductController;
import model.dto.ProductResponseDto;
import model.entities.Product;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ProductView {
    private final static ProductController productController = new ProductController();
    private static void thumbnail(){
        System.out.println("""
                1. Find Product By Product Name
                2. Find Product By Product Category
                3. Exit
                """);
    };
    public static void home(){
        while(true){
            thumbnail();
            System.out.println("[+] Insert option: ");
            switch (new Scanner(System.in).nextInt()){
                case 1->{
                    System.out.println("Enter Product Name: ");
                    List<ProductResponseDto> productResponseDtoList =productController.getProductByName(new  Scanner(System.in).nextLine());
                    Collections.reverse(productResponseDtoList);
                    productResponseDtoList
                            .forEach(System.out::println);
                }
                case 2->{
                    System.out.println("Enter Product Category: ");
                    List<ProductResponseDto> productResponseDtoList =productController.getProductByCategory(new  Scanner(System.in).nextLine());
                    Collections.reverse(productResponseDtoList);
                    productResponseDtoList.forEach(System.out::println);
                }
                case 3->{
                    System.exit(0);
                }
            }
        }
    }
}
