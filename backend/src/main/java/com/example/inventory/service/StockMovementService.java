package com.example.inventory.service;

import com.example.inventory.model.MovementType;
import com.example.inventory.model.StockMovement;
import com.example.inventory.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class StockMovementService {
    private static final Sort TABLE_SORT = Sort.by(Sort.Direction.DESC, "timestamp").and(Sort.by("id"));
    private static final Sort EXPORT_SORT = Sort.by(Sort.Direction.ASC, "timestamp").and(Sort.by("id"));

    private final StockMovementRepository repository;

    public StockMovementService(StockMovementRepository repository) {
        this.repository = repository;
    }

    public Page<StockMovement> findMovementPage(MovementFilter filter, int page, int size) {
        Instant fromInstant = filter.fromInstant();
        Instant toExclusive = filter.toExclusiveInstant();
        Pageable pageable = PageRequest.of(page, size, TABLE_SORT);

        return filter.type() == null
                ? repository.findByTimestampGreaterThanEqualAndTimestampLessThan(fromInstant, toExclusive, pageable)
                : repository.findByTimestampGreaterThanEqualAndTimestampLessThanAndMovementType(
                fromInstant,
                toExclusive,
                filter.type(),
                pageable
        );
    }

    public MovementAnalytics findMovementAnalytics(MovementFilter filter) {
        List<StockMovement> allFiltered = findAllFiltered(filter);

        return new MovementAnalytics(
                buildSummary(allFiltered),
                buildDailyTotals(allFiltered)
        );
    }

    public List<StockMovement> findMovementsForExport(MovementFilter filter) {
        return findAllFiltered(filter);
    }

    private List<StockMovement> findAllFiltered(MovementFilter filter) {
        Instant fromInstant = filter.fromInstant();
        Instant toExclusive = filter.toExclusiveInstant();

        return filter.type() == null
                ? repository.findByTimestampGreaterThanEqualAndTimestampLessThan(fromInstant, toExclusive, EXPORT_SORT)
                : repository.findByTimestampGreaterThanEqualAndTimestampLessThanAndMovementType(
                fromInstant,
                toExclusive,
                filter.type(),
                EXPORT_SORT
        );
    }

    private MovementSummary buildSummary(List<StockMovement> movements) {
        long inQuantity = movements.stream()
                .filter(movement -> movement.getMovementType() == MovementType.IN)
                .mapToLong(StockMovement::getQuantity)
                .sum();
        long outQuantity = movements.stream()
                .filter(movement -> movement.getMovementType() == MovementType.OUT)
                .mapToLong(StockMovement::getQuantity)
                .sum();

        return new MovementSummary(inQuantity, outQuantity);
    }

    private List<DailyMovementTotal> buildDailyTotals(List<StockMovement> movements) {
        Map<LocalDate, DailyAccumulator> totalsByDate = new TreeMap<>();

        for (StockMovement movement : movements) {
            LocalDate date = movement.getTimestamp().atZone(java.time.ZoneOffset.UTC).toLocalDate();
            DailyAccumulator accumulator = totalsByDate.computeIfAbsent(date, ignored -> new DailyAccumulator());
            if (movement.getMovementType() == MovementType.IN) {
                accumulator.inQuantity += movement.getQuantity();
            } else {
                accumulator.outQuantity += movement.getQuantity();
            }
        }

        return totalsByDate.entrySet().stream()
                .map(entry -> new DailyMovementTotal(
                        entry.getKey(),
                        entry.getValue().inQuantity,
                        entry.getValue().outQuantity
                ))
                .toList();
    }

    private static class DailyAccumulator {
        private long inQuantity;
        private long outQuantity;
    }
}
