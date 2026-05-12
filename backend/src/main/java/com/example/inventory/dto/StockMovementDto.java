package com.example.inventory.dto;

import com.example.inventory.model.MovementType;
import com.example.inventory.model.StockMovement;

import java.time.Instant;

public record StockMovementDto(
        String id,
        Instant timestamp,
        String sku,
        MovementType movementType,
        int quantity
) {
    public static StockMovementDto fromEntity(StockMovement movement) {
        return new StockMovementDto(
                movement.getId(),
                movement.getTimestamp(),
                movement.getSku(),
                movement.getMovementType(),
                movement.getQuantity()
        );
    }
}
