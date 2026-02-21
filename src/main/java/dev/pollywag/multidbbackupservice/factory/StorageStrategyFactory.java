package dev.pollywag.multidbbackupservice.factory;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import dev.pollywag.multidbbackupservice.strategy.storage.LocalStorageStrategy;
import dev.pollywag.multidbbackupservice.strategy.storage.S3StorageStrategy;
import dev.pollywag.multidbbackupservice.strategy.storage.StorageStrategy;
import org.springframework.stereotype.Component;

@Component
public class StorageStrategyFactory {
    private final LocalStorageStrategy localStorage;
    private final S3StorageStrategy s3Storage;

    public StorageStrategyFactory(LocalStorageStrategy localStorage, S3StorageStrategy s3Storage) {
        this.localStorage = localStorage;
        this.s3Storage = s3Storage;
    }

    public StorageStrategy getStrategy(StorageType type) {
        return switch (type) {
            case LOCAL -> localStorage;
            case AWS_S3 -> s3Storage;
            default -> throw new BackupException("Unsupported Storage type: " + type);
        };
    }
}
