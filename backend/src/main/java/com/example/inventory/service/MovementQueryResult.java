package com.example.inventory.service;

import com.example.inventory.dto.DailyMovementTotalDto;
import com.example.inventory.dto.MovementSummaryDto;
import com.example.inventory.dto.StockMovementDto;

import java.util.List;

public record MovementQueryResult(
        List<StockMovementDto> pageContent,
        int page,
        int size,
        long totalElements,
        int totalPages,
        MovementSummaryDto summary,
        List<DailyMovementTotalDto> dailyTotals,
        List<StockMovementDto> allFilteredContent
) {
}
