package com.example.inventory.dto;

import java.time.LocalDate;

public record DailyMovementTotalDto(
        LocalDate date,
        long inQuantity,
        long outQuantity
) {
}
