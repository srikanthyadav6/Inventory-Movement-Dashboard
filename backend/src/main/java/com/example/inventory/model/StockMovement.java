package com.example.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "stock_movements",
        indexes = {
                @Index(name = "idx_stock_movements_timestamp", columnList = "timestamp"),
                @Index(name = "idx_stock_movements_type", columnList = "movement_type")
        }
)
public class StockMovement {
    @Id
    private String id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private int quantity;

    protected StockMovement() {
    }

    public StockMovement(String id, Instant timestamp, String sku, MovementType movementType, int quantity) {
        this.id = id;
        this.timestamp = timestamp;
        this.sku = sku;
        this.movementType = movementType;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSku() {
        return sku;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public int getQuantity() {
        return quantity;
    }
}
