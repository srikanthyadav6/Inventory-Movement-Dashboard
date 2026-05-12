package com.example.inventory.service;

import com.example.inventory.dto.DailyMovementTotalDto;
import com.example.inventory.dto.MovementSummaryDto;
import com.example.inventory.dto.StockMovementDto;
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
import java.time.ZoneOffset;
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

    public MovementQueryResult findMovements(LocalDate from, LocalDate to, MovementType type, int page, int size) {
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toExclusive = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Pageable pageable = PageRequest.of(page, size, TABLE_SORT);

        Page<StockMovement> pageResult = type == null
                ? repository.findByTimestampGreaterThanEqualAndTimestampLessThan(fromInstant, toExclusive, pageable)
                : repository.findByTimestampGreaterThanEqualAndTimestampLessThanAndMovementType(fromInstant, toExclusive, type, pageable);

        List<StockMovement> allFiltered = type == null
                ? repository.findByTimestampGreaterThanEqualAndTimestampLessThan(fromInstant, toExclusive, EXPORT_SORT)
                : repository.findByTimestampGreaterThanEqualAndTimestampLessThanAndMovementType(fromInstant, toExclusive, type, EXPORT_SORT);

        List<StockMovementDto> allFilteredDtos = allFiltered.stream()
                .map(StockMovementDto::fromEntity)
                .toList();

        return new MovementQueryResult(
                pageResult.getContent().stream().map(StockMovementDto::fromEntity).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                buildSummary(allFiltered),
                buildDailyTotals(allFiltered),
                allFilteredDtos
        );
    }

    private MovementSummaryDto buildSummary(List<StockMovement> movements) {
        long inQuantity = movements.stream()
                .filter(movement -> movement.getMovementType() == MovementType.IN)
                .mapToLong(StockMovement::getQuantity)
                .sum();
        long outQuantity = movements.stream()
                .filter(movement -> movement.getMovementType() == MovementType.OUT)
                .mapToLong(StockMovement::getQuantity)
                .sum();

        return new MovementSummaryDto(inQuantity, outQuantity);
    }

    private List<DailyMovementTotalDto> buildDailyTotals(List<StockMovement> movements) {
        Map<LocalDate, DailyAccumulator> totalsByDate = new TreeMap<>();

        for (StockMovement movement : movements) {
            LocalDate date = movement.getTimestamp().atZone(ZoneOffset.UTC).toLocalDate();
            DailyAccumulator accumulator = totalsByDate.computeIfAbsent(date, ignored -> new DailyAccumulator());
            if (movement.getMovementType() == MovementType.IN) {
                accumulator.inQuantity += movement.getQuantity();
            } else {
                accumulator.outQuantity += movement.getQuantity();
            }
        }

        return totalsByDate.entrySet().stream()
                .map(entry -> new DailyMovementTotalDto(
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
