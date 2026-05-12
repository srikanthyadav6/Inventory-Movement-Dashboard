package com.example.inventory.web;

import com.example.inventory.dto.MovementPageResponse;
import com.example.inventory.dto.StockMovementDto;
import com.example.inventory.model.MovementType;
import com.example.inventory.service.MovementQueryResult;
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
        MovementQueryResult result = service.findMovements(from, to, type, normalizedPage, normalizedSize);

        if (export) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename("stock-movements.csv")
                            .build()
                            .toString())
                    .body(toCsv(result));
        }

        return ResponseEntity.ok(new MovementPageResponse(
                result.pageContent(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.summary(),
                result.dailyTotals()
        ));
    }

    private String toCsv(MovementQueryResult result) {
        StringBuilder builder = new StringBuilder("id,timestamp,sku,movementType,quantity\n");
        for (StockMovementDto movement : result.allFilteredContent()) {
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
