package view;

import model.dto.ProductDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductView {

    private int[] calculateMaxLengths(List<ProductDto> products) {
        int maxIdLen = "ID".length();
        int maxNameLen = "Product Name".length();
        int maxPriceLen = "Price".length();
        int maxQtyLen = "Qty".length();

        for (ProductDto product : products) {
            maxIdLen = Math.max(maxIdLen, String.valueOf(product.getId()).length());
            maxNameLen = Math.max(maxNameLen, product.getProductName() != null ? product.getProductName().length() : 0);
            maxPriceLen = Math.max(maxPriceLen, String.format("%.2f", product.getPrice() != null ? product.getPrice() : 0.0).length());
            maxQtyLen = Math.max(maxQtyLen, String.valueOf(product.getQuantity()).length());
        }
        return new int[]{maxIdLen, maxNameLen, maxPriceLen, maxQtyLen};
    }

    private void printProductTable(String title, List<ProductDto> products, int[] maxLengths) {
        if (products == null || products.isEmpty()) {
            System.out.println(title + " (No products found in this list).");
            return;
        }

        int idLen = maxLengths[0];
        int nameLen = maxLengths[1];
        int priceLen = maxLengths[2];
        int qtyLen = maxLengths[3];

        String topBorder = "┌" + "─".repeat(idLen + 2) + "┬" + "─".repeat(nameLen + 2) + "┬" + "─".repeat(priceLen + 2) + "┬" + "─".repeat(qtyLen + 2) + "┐";
        String midBorder = "├" + "─".repeat(idLen + 2) + "┼" + "─".repeat(nameLen + 2) + "┼" + "─".repeat(priceLen + 2) + "┼" + "─".repeat(qtyLen + 2) + "┤";
        String bottomBorder = "└" + "─".repeat(idLen + 2) + "┴" + "─".repeat(nameLen + 2) + "┴" + "─".repeat(priceLen + 2) + "┴" + "─".repeat(qtyLen + 2) + "┘";

        System.out.println("\n📦 " + title);
        System.out.println(topBorder);
        System.out.printf("│ %-" + idLen + "s │ %-" + nameLen + "s │ %" + priceLen + "s │ %" + qtyLen + "s │%n",
                "ID", "Product Name", "Price", "Qty");
        System.out.println(midBorder);

        for (ProductDto product : products) {
            System.out.printf("│ %-" + idLen + "d │ %-" + nameLen + "s │ %" + priceLen + ".2f │ %" + qtyLen + "d │%n",
                    product.getId() != null ? product.getId() : 0L,
                    product.getProductName() != null ? product.getProductName() : "N/A",
                    product.getPrice() != null ? product.getPrice() : 0.0,
                    product.getQuantity() != null ? product.getQuantity() : 0);
        }

        System.out.println(bottomBorder);
    }

    public void displayAllProducts(List<ProductDto> allProducts) {
        System.out.println("\n🛍️  --- ALL PRODUCTS IN STORE ---");
        if (allProducts == null || allProducts.isEmpty()) {
            System.out.println("No active products found in the store.");
            return;
        }

        int[] maxLengths = calculateMaxLengths(allProducts);
        printProductTable("All Products", allProducts, maxLengths);
        System.out.println("✅ End of product list\n");
    }

    public void displayProductsByCategory(Map<String, List<ProductDto>> productsByCategory) {
        System.out.println("\n📂 --- PRODUCTS BY CATEGORY ---");
        if (productsByCategory == null || productsByCategory.isEmpty() ||
                productsByCategory.values().stream().allMatch(List::isEmpty)) {
            System.out.println("No products found to display by category.");
            return;
        }

        List<ProductDto> allProducts = productsByCategory.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (allProducts.isEmpty()) {
            System.out.println("No products found to display by category.");
            return;
        }

        int[] maxLengths = calculateMaxLengths(allProducts);

        productsByCategory.forEach((category, products) -> {
            printProductTable("Category: " + category, products, maxLengths);
        });

        System.out.println("✅ End of categorized product list\n");
    }

    public void displayNoProductsFound() {
        System.out.println("⚠️  No active products found in the store.");
    }
}
