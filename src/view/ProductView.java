package view;

import controller.ProductController;
import model.dto.ProductResponseDto;
import model.entities.User;
import model.repository.UserRepositoryImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ProductView {
    private final static ProductController productController = new ProductController();
    private final static UserRepositoryImpl userRepository = new UserRepositoryImpl();
    private static final User user = userRepository.getLoggedInUser();
    private static void searchProductThumbnail() {
        System.out.println("""
                ╔═══════════════════════════════════════════════════════════════════════╗
                ║                               SEARCH PRODUCT                          ║
                ╟═══════════════════════════════════════════════════════════════════════╢
                ║1. Find Product By Product Name                                        ║
                ║2. Find Product By Product Category                                    ║
                ║0. Exit                                                                ║
                ╚═══════════════════════════════════════════════════════════════════════╝
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
                System.out.println();
            } else {
                System.out.println("🗂️  Category: " + category + " (⚠️ products in this category)");
            }
        });
        System.out.println("✅ Done listing products by category.\n");
    }

    public static void searchProduct() {
        boolean isExited = false;
        do {
            searchProductThumbnail();
            System.out.print("[+] Insert your option: ");
            try {
                byte option = new Scanner(System.in).nextByte();
                switch (option) {
                    case 1 -> {
                        System.out.println("[+] Enter Product Name: ");
                        List<ProductResponseDto> productResponseDtoList = productController.getProductByName(new Scanner(System.in).nextLine());
                        Collections.reverse(productResponseDtoList);
                        TableUI<ProductResponseDto> tableUI = new TableUI<>();
                        tableUI.getTableDisplay(productResponseDtoList);
                        return;
                    }
                    case 2 -> {
                        System.out.println("[+] Enter Product Category: ");
                        List<ProductResponseDto> productResponseDtoList = productController.getProductByCategory(new Scanner(System.in).nextLine());
                        Collections.reverse(productResponseDtoList);
                        TableUI<ProductResponseDto> tableUI = new TableUI<>();
                        tableUI.getTableDisplay(productResponseDtoList);
                        return;
                    }
                    case 0 -> isExited = true;
                    default -> System.out.println("⚠️ Invalid option. Choose 1 or 2 (0 to exit)");
                }
            } catch (Exception e) {
                System.out.println("[!] Insert number only");
            }
        } while (!isExited);
    }

    private static void addProductThumbnail() {
        System.out.println("""
                ╔═══════════════════════════════════════════════════════════════════════╗
                ║                               SEARCH PRODUCT                          ║
                ╟═══════════════════════════════════════════════════════════════════════╢
                ║1. Search Product                                                      ║
                ║2. View All Product                                                    ║
                ║0. Exit                                                                ║
                ╚═══════════════════════════════════════════════════════════════════════╝
                """);
    };

    public static void addProductToCart() {
        boolean isExited = false;
        do {
            addProductThumbnail();
            System.out.print("[+] Insert your option: ");
            try {
                byte option = new Scanner(System.in).nextByte();
                switch (option) {
                    case 1 -> {
                        searchProduct();
                        return;
                    }
                    case 2 -> {
                        listAllProductsInStoreSeparatedByCategory();
                        return;
                    }
                    case 0 -> isExited = true;
                    default -> {
                        System.out.println("⚠️ Invalid option. Choose 1 or 2 (0 to exit)");
                        continue;
                    }
                }
                System.out.println();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } while (!isExited);
    }
}