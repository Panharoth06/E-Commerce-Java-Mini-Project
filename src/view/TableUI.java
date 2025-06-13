package view;

import model.dto.CartResponseDto;
import model.dto.ProductResponseDto;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.List;

public class TableUI<T> {

    private final CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);

    public void getTableDisplay(List<T> tList) {
        if (tList == null || tList.isEmpty()) {
            System.out.println("⚠️  No data available to display.");
            return;
        }

        T firstItem = tList.getFirst();
        String[] columnNames;
        Table table;

        if (firstItem instanceof ProductResponseDto) {
            columnNames = new String[]{"UUID", "PRODUCT NAME", "PRICE", "QUANTITY", "CATEGORY"};
            table = new Table(columnNames.length, BorderStyle.UNICODE_BOX_DOUBLE_BORDER, ShownBorders.ALL);

            // Header
            for (String column : columnNames) {
                table.addCell(column, center);
            }

            // Rows
            for (T t : tList) {
                ProductResponseDto product = (ProductResponseDto) t;
                table.addCell(product.productUuid(), center);
                table.addCell(product.productName(), center);
                table.addCell(String.format("%.2f", product.price() != null ? product.price() : 0.0), center);
                table.addCell(String.valueOf(product.quantity() != null ? product.quantity() : 0), center);
                table.addCell(product.category(), center);
            }

        } else if (firstItem instanceof CartResponseDto) {
            columnNames = new String[]{"UUID", "PRODUCT NAME", "QUANTITY", "CATEGORY"};
            table = new Table(columnNames.length, BorderStyle.UNICODE_BOX_DOUBLE_BORDER, ShownBorders.ALL);

            for (String column : columnNames) {
                table.addCell(column, center);
            }

            for (T t : tList) {
                CartResponseDto cart = (CartResponseDto) t;
                table.addCell(cart.productUUID(), center);
                table.addCell(cart.productName(), center);
                table.addCell(String.valueOf(cart.quantity()), center);
                table.addCell(cart.category(), center);
            }

        } else {
            System.out.println("⚠️  Unsupported data type: " + firstItem.getClass().getSimpleName());
            return;
        }

        for (int i = 0; i < columnNames.length; i++) {
            table.setColumnWidth(i, 30, 50);
        }

        System.out.println(table.render());
    }
}
