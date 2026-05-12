package com.example.inventory.data;

import com.example.inventory.model.MovementType;
import com.example.inventory.model.StockMovement;
import com.example.inventory.repository.StockMovementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
public class StockMovementDataLoader implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final StockMovementRepository repository;

    public StockMovementDataLoader(ObjectMapper objectMapper, StockMovementRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws IOException {
        if (repository.count() > 0) {
            return;
        }

        Path mockDataPath = resolveMockDataPath();
        SeedMovement[] seedMovements = objectMapper.readValue(Files.readString(mockDataPath), SeedMovement[].class);
        List<StockMovement> movements = Arrays.stream(seedMovements)
                .map(item -> new StockMovement(
                        item.id(),
                        item.timestamp(),
                        item.sku(),
                        item.movementType(),
                        item.quantity()
                ))
                .toList();

        repository.saveAll(movements);
    }

    private Path resolveMockDataPath() {
        Path workingDirectory = Path.of("").toAbsolutePath();
        List<Path> candidates = List.of(
                workingDirectory.resolve("mock_movements.json"),
                workingDirectory.resolve("..").resolve("mock_movements.json").normalize()
        );

        return candidates.stream()
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("mock_movements.json was not found in the repo root."));
    }

    private record SeedMovement(
            String id,
            Instant timestamp,
            String sku,
            MovementType movementType,
            int quantity
    ) {
    }
}
