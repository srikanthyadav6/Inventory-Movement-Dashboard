package com.example.inventory.service;

import java.util.List;

public record MovementAnalytics(
        MovementSummary summary,
        List<DailyMovementTotal> dailyTotals
) {
}
