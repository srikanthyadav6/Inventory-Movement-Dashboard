package com.example.inventory.service;

import java.time.LocalDate;

public record DailyMovementTotal(
        LocalDate date,
        long inQuantity,
        long outQuantity
) {
}
