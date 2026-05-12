package com.example.inventory.dto;

import java.util.List;

public record MovementPageResponse(
        List<StockMovementDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
