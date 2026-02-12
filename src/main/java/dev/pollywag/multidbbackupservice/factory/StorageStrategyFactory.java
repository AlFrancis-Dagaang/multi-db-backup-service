package dev.pollywag.multidbbackupservice.factory;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import dev.pollywag.multidbbackupservice.strategy.storage.LocalStorageStrategy;
import dev.pollywag.multidbbackupservice.strategy.storage.StorageStrategy;
import org.springframework.stereotype.Component;

@Component
public class StorageStrategyFactory {
    private final LocalStorageStrategy localStorage;

    public StorageStrategyFactory(LocalStorageStrategy localStorage) {
        this.localStorage = localStorage;
    }

    public StorageStrategy getStrategy(StorageType type) {
        return switch(type) {
            case LOCAL -> localStorage;
            default -> throw new BackupException("Unsupported Storage type: " + type);
        };
    }
}
