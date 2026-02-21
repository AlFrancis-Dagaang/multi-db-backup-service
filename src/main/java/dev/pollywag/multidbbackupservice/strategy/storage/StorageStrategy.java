package dev.pollywag.multidbbackupservice.strategy.storage;

import dev.pollywag.multidbbackupservice.model.request.BackupRequest;

import java.io.File;

public interface StorageStrategy {
    /**
     * Store the backup file and return a storage path (local path or s3://bucket/key).
     */
    String store(File file, BackupRequest request);

    /**
     * Resolve the stored path to a local File for restore.
     * For LOCAL: returns the file at the path.
     * For S3: downloads to a temp file and returns it (caller should delete when done).
     */
    File resolveToLocalFile(String storagePath);
}
