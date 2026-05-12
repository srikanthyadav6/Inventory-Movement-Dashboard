package com.example.inventory.web;

import com.example.inventory.dto.DailyMovementTotalDto;
import com.example.inventory.dto.MovementAnalyticsResponse;
import com.example.inventory.dto.MovementPageResponse;
import com.example.inventory.dto.MovementSummaryDto;
import com.example.inventory.dto.StockMovementDto;
import com.example.inventory.model.StockMovement;
import com.example.inventory.service.MovementAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class MovementResponseMapper {
    public MovementPageResponse toPageResponse(Page<StockMovement> page) {
        return new MovementPageResponse(
                page.getContent().stream().map(StockMovementDto::fromEntity).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public MovementAnalyticsResponse toAnalyticsResponse(MovementAnalytics analytics) {
        return new MovementAnalyticsResponse(
                new MovementSummaryDto(
                        analytics.summary().inQuantity(),
                        analytics.summary().outQuantity()
                ),
                analytics.dailyTotals().stream()
                        .map(total -> new DailyMovementTotalDto(
                                total.date(),
                                total.inQuantity(),
                                total.outQuantity()
                        ))
                        .toList()
        );
    }
}
