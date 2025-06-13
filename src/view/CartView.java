package view;

import model.dto.CartDto;
import java.util.List;

public class CartView {

    public void displayCart(List<CartDto> cartItems) {
        System.out.println("\n🛒 YOUR SHOPPING CART");
        if (cartItems == null || cartItems.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        // Calculate max lengths for formatting
        int maxIdLen = "Item ID".length();
        int maxProductIdLen = "Product ID".length();
        int maxNameLen = "Product Name".length();
        int maxCategoryLen = "Category".length();
        int maxUnitPriceLen = "Unit Price".length();
        int maxQtyLen = "Qty".length();
        int maxTotalLen = "Total Price".length();

        for (CartDto item : cartItems) {
            maxIdLen = Math.max(maxIdLen, String.valueOf(item.getCartItemId()).length());
            maxProductIdLen = Math.max(maxProductIdLen, String.valueOf(item.getProductId()).length());
            maxNameLen = Math.max(maxNameLen, item.getProductName() != null ? item.getProductName().length() : 0);
            maxCategoryLen = Math.max(maxCategoryLen, item.getCategory() != null ? item.getCategory().length() : 0);
            maxUnitPriceLen = Math.max(maxUnitPriceLen, String.format("%.2f", item.getUnitPrice()).length());
            maxQtyLen = Math.max(maxQtyLen, String.valueOf(item.getQuantity()).length());
            maxTotalLen = Math.max(maxTotalLen, String.format("%.2f", item.getTotalPrice()).length());
        }

        String format = "│ %-" + maxIdLen + "s │ %-" + maxProductIdLen + "s │ %-" + maxNameLen + "s │ %-" + maxCategoryLen + "s │ %"
                + maxUnitPriceLen + "s │ %" + maxQtyLen + "s │ %" + maxTotalLen + "s │";

        String topBorder = "┌" + repeat("─", maxIdLen + 2) + "┬" + repeat("─", maxProductIdLen + 2) + "┬" + repeat("─", maxNameLen + 2)
                + "┬" + repeat("─", maxCategoryLen + 2) + "┬" + repeat("─", maxUnitPriceLen + 2) + "┬" + repeat("─", maxQtyLen + 2)
                + "┬" + repeat("─", maxTotalLen + 2) + "┐";
        String separator = "├" + repeat("─", maxIdLen + 2) + "┼" + repeat("─", maxProductIdLen + 2) + "┼" + repeat("─", maxNameLen + 2)
                + "┼" + repeat("─", maxCategoryLen + 2) + "┼" + repeat("─", maxUnitPriceLen + 2) + "┼" + repeat("─", maxQtyLen + 2)
                + "┼" + repeat("─", maxTotalLen + 2) + "┤";
        String bottomBorder = "└" + repeat("─", maxIdLen + 2) + "┴" + repeat("─", maxProductIdLen + 2) + "┴" + repeat("─", maxNameLen + 2)
                + "┴" + repeat("─", maxCategoryLen + 2) + "┴" + repeat("─", maxUnitPriceLen + 2) + "┴" + repeat("─", maxQtyLen + 2)
                + "┴" + repeat("─", maxTotalLen + 2) + "┘";

        System.out.println(topBorder);
        System.out.printf(format, "Item ID", "Product ID", "Product Name", "Category", "Unit Price", "Qty", "Total Price");
        System.out.println();
        System.out.println(separator);

        double grandTotal = 0.0;
        for (CartDto item : cartItems) {
            System.out.printf(format,
                    item.getCartItemId(),
                    item.getProductId(),
                    item.getProductName() != null ? item.getProductName() : "N/A",
                    item.getCategory() != null ? item.getCategory() : "N/A",
                    String.format("%.2f", item.getUnitPrice() != null ? item.getUnitPrice() : 0.0),
                    item.getQuantity(),
                    String.format("%.2f", item.getTotalPrice() != null ? item.getTotalPrice() : 0.0));
            System.out.println();

            grandTotal += item.getTotalPrice() != null ? item.getTotalPrice() : 0.0;
        }

        System.out.println(bottomBorder);
        System.out.printf("💵 GRAND TOTAL: %.2f\n", grandTotal);
        System.out.println("🛍️  --- End of Cart ---");
    }

    private String repeat(String s, int times) {
        return s.repeat(times);
    }

    public void displayMessage(String message) {
        System.out.println("🟢 " + message);
    }

    public void displayErrorMessage(String errorMessage) {
        System.err.println("🔴 Error: " + errorMessage);
    }
}
