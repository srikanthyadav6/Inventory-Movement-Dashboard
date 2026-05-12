package com.example.inventory.web;

import com.example.inventory.model.MovementType;
import com.example.inventory.service.MovementFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Component
public class MovementRequestSupport {
    public MovementFilter toFilter(LocalDate from, LocalDate to, MovementType type) {
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The to date must be on or after the from date.");
        }

        return new MovementFilter(from, to, type);
    }

    public int normalizePage(int page) {
        return Math.max(page, 0);
    }

    public int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }
}
