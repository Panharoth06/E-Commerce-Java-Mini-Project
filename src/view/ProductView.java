package view;

import controller.ProductController;
import model.dto.ProductResponseDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ProductView {
    private final static ProductController productController = new ProductController();

    private static void thumbnail() {
        System.out.println("""
                +====================== | SEARCH PRODUCT | ======================+
                |1. Find Product By Product Name                                 |
                |2. Find Product By Product Category                             |
                |3. Exit                                                         |
                +================================================================+
                """);
    };

    public static void listAllProductsInStoreSeparatedByCategory() {
        Map<String, List<ProductResponseDto>> productsByCategory = productController.listAllProductsInStoreSeparatedByCategory();

        if (productsByCategory == null || productsByCategory.isEmpty()) {
            System.out.println("⚠️  No products found in store.");
            return;
        }

        TableUI<ProductResponseDto> tableUI = new TableUI<>();

        System.out.println("\n📦 PRODUCTS BY CATEGORY:\n");

        productsByCategory.forEach((category, products) -> {
            if (products != null && !products.isEmpty()) {
                System.out.println("🗂️  Category: " + category);
                tableUI.getTableDisplay(products);
                System.out.println(); // extra space between categories
            } else {
                System.out.println("🗂️  Category: " + category + " (No products in this category)");
            }
        });

        System.out.println("✅ Done listing products by category.\n");
    }


    public static void searchProduct() {
        boolean isExited = false;
        do {
            thumbnail();
            System.out.print("[+] Insert your option: ");
            try {
                byte option = new Scanner(System.in).nextByte();
                switch (option) {
                    case 1 -> {
                        System.out.println("[+] Enter Product Name: ");
                        List<ProductResponseDto> productResponseDtoList = productController.getProductByName(new Scanner(System.in).nextLine());
                        Collections.reverse(productResponseDtoList);
                        productResponseDtoList
                                .forEach(System.out::println);
                    }
                    case 2 -> {
                        System.out.println("[+] Enter Product Category: ");
                        List<ProductResponseDto> productResponseDtoList = productController.getProductByCategory(new Scanner(System.in).nextLine());
                        Collections.reverse(productResponseDtoList);
                        productResponseDtoList.forEach(System.out::println);
                    }
                    case 3 -> isExited = true;

                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } while (!isExited);
    }
}