package com.example.inventory.repository;

import com.example.inventory.model.MovementType;
import com.example.inventory.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, String> {
    Page<StockMovement> findByTimestampGreaterThanEqualAndTimestampLessThan(
            Instant from,
            Instant toExclusive,
            Pageable pageable
    );

    Page<StockMovement> findByTimestampGreaterThanEqualAndTimestampLessThanAndMovementType(
            Instant from,
            Instant toExclusive,
            MovementType movementType,
            Pageable pageable
    );

    List<StockMovement> findByTimestampGreaterThanEqualAndTimestampLessThan(
            Instant from,
            Instant toExclusive,
            Sort sort
    );

    List<StockMovement> findByTimestampGreaterThanEqualAndTimestampLessThanAndMovementType(
            Instant from,
            Instant toExclusive,
            MovementType movementType,
            Sort sort
    );
}
