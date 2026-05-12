package com.example.inventory.service;

import com.example.inventory.model.MovementType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record MovementFilter(
        LocalDate from,
        LocalDate to,
        MovementType type
) {
    public Instant fromInstant() {
        return from.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public Instant toExclusiveInstant() {
        return to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
