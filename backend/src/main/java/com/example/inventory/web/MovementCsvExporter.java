package com.example.inventory.web;

import com.example.inventory.model.StockMovement;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovementCsvExporter {
    public String toCsv(List<StockMovement> movements) {
        StringBuilder builder = new StringBuilder("id,timestamp,sku,movementType,quantity\n");
        for (StockMovement movement : movements) {
            builder.append(csv(movement.getId())).append(',')
                    .append(csv(movement.getTimestamp().toString())).append(',')
                    .append(csv(movement.getSku())).append(',')
                    .append(csv(movement.getMovementType().name())).append(',')
                    .append(movement.getQuantity())
                    .append('\n');
        }
        return builder.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }

        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }
}
