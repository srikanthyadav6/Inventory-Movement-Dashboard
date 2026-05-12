package com.example.inventory.dto;

public record MovementSummaryDto(
        long inQuantity,
        long outQuantity
) {
}
