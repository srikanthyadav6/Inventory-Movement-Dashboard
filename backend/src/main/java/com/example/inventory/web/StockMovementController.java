package com.example.inventory.web;

import com.example.inventory.dto.MovementAnalyticsResponse;
import com.example.inventory.dto.MovementPageResponse;
import com.example.inventory.dto.StockMovementDto;
import com.example.inventory.model.MovementType;
import com.example.inventory.service.StockMovementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movements")
public class StockMovementController {
    private final StockMovementService service;

    public StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getMovements(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) MovementType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean export
    ) {
        if (to.isBefore(from)) {
            return ResponseEntity.badRequest().body("The to date must be on or after the from date.");
        }

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 100);
        if (export) {
            List<StockMovementDto> movements = service.findMovementsForExport(from, to, type);
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename("stock-movements.csv")
                            .build()
                            .toString())
                    .body(toCsv(movements));
        }

        return ResponseEntity.ok(service.findMovementPage(from, to, type, normalizedPage, normalizedSize));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getMovementAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) MovementType type
    ) {
        if (to.isBefore(from)) {
            return ResponseEntity.badRequest().body("The to date must be on or after the from date.");
        }

        MovementAnalyticsResponse analytics = service.findMovementAnalytics(from, to, type);
        return ResponseEntity.ok(analytics);
    }

    private String toCsv(List<StockMovementDto> movements) {
        StringBuilder builder = new StringBuilder("id,timestamp,sku,movementType,quantity\n");
        for (StockMovementDto movement : movements) {
            builder.append(csv(movement.id())).append(',')
                    .append(csv(movement.timestamp().toString())).append(',')
                    .append(csv(movement.sku())).append(',')
                    .append(csv(movement.movementType().name())).append(',')
                    .append(movement.quantity())
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
