package com.example.inventory.dto;

import java.util.List;

public record MovementAnalyticsResponse(
        MovementSummaryDto summary,
        List<DailyMovementTotalDto> dailyTotals
) {
}
