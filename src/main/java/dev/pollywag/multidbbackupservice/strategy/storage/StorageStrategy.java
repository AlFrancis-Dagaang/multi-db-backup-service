package dev.pollywag.multidbbackupservice.strategy.storage;

import dev.pollywag.multidbbackupservice.model.request.BackupRequest;

import java.io.File;

public interface StorageStrategy {
    String store(File file, BackupRequest request);
}
