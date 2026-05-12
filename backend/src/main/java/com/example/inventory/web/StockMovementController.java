package com.example.inventory.web;

import com.example.inventory.model.MovementType;
import com.example.inventory.service.MovementFilter;
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
    private final MovementRequestSupport requestSupport;
    private final MovementResponseMapper responseMapper;
    private final MovementCsvExporter csvExporter;

    public StockMovementController(
            StockMovementService service,
            MovementRequestSupport requestSupport,
            MovementResponseMapper responseMapper,
            MovementCsvExporter csvExporter
    ) {
        this.service = service;
        this.requestSupport = requestSupport;
        this.responseMapper = responseMapper;
        this.csvExporter = csvExporter;
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
        MovementFilter filter = requestSupport.toFilter(from, to, type);

        if (export) {
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename("stock-movements.csv")
                            .build()
                            .toString())
                    .body(csvExporter.toCsv(service.findMovementsForExport(filter)));
        }

        return ResponseEntity.ok(responseMapper.toPageResponse(service.findMovementPage(
                filter,
                requestSupport.normalizePage(page),
                requestSupport.normalizeSize(size)
        )));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getMovementAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) MovementType type
    ) {
        MovementFilter filter = requestSupport.toFilter(from, to, type);
        return ResponseEntity.ok(responseMapper.toAnalyticsResponse(service.findMovementAnalytics(filter)));
    }
}
