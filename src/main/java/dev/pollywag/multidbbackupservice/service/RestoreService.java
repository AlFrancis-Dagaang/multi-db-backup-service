package dev.pollywag.multidbbackupservice.service;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.factory.DatabaseStrategyFactory;
import dev.pollywag.multidbbackupservice.factory.StorageStrategyFactory;
import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import dev.pollywag.multidbbackupservice.model.entity.BackupMetadata;
import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import dev.pollywag.multidbbackupservice.model.request.RestoreRequest;
import dev.pollywag.multidbbackupservice.model.response.RestoreResponse;
import dev.pollywag.multidbbackupservice.repository.BackupMetadataRepository;
import dev.pollywag.multidbbackupservice.strategy.database.DatabaseBackupStrategy;
import dev.pollywag.multidbbackupservice.strategy.storage.StorageStrategy;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class RestoreService {

    private final BackupMetadataRepository metadataRepository;
    private final DatabaseStrategyFactory dbFactory;
    private final StorageStrategyFactory storageFactory;
    private final LogService logService;

    public RestoreService(BackupMetadataRepository metadataRepository,
                          DatabaseStrategyFactory dbFactory,
                          StorageStrategyFactory storageFactory,
                          LogService logService) {
        this.metadataRepository = metadataRepository;
        this.dbFactory = dbFactory;
        this.storageFactory = storageFactory;
        this.logService = logService;
    }

    public RestoreResponse restore(RestoreRequest request) {

        // Start log
        BackupLog log = logService.start("RESTORE", request.getBackupId());

        try {
            // 1. Load metadata for the requested backupId
            BackupMetadata metadata = metadataRepository.findById(request.getBackupId())
                    .orElseThrow(() -> new BackupException(
                            "No backup found with id: " + request.getBackupId()));

            // 2. Fill in target credentials — fall back to original DB info if caller
            //    didn't override them (handy for restoring to the same host)
            resolveTargetCredentials(request, metadata);

            // 3. Get the right DB strategy (MYSQL, POSTGRESQL, etc.)
            DatabaseBackupStrategy strategy = dbFactory.getStrategy(metadata.getDbType());

            // 4. If this is an INCREMENTAL backup, restore the parent FULL backup first
            if (metadata.getBackupType() == BackupType.INCREMENTAL) {

                if (metadata.getParentBackupId() == null) {
                    throw new BackupException(
                            "Incremental backup " + metadata.getBackupId() +
                                    " has no parentBackupId — cannot restore without base.");
                }

                BackupMetadata parentMetadata = metadataRepository
                        .findById(metadata.getParentBackupId())
                        .orElseThrow(() -> new BackupException(
                                "Parent full backup not found: " + metadata.getParentBackupId()));

                // Restore the parent FULL backup first
                restoreSingle(strategy, parentMetadata, request);
            }

            // 5. Restore the requested backup (full or the incremental on top of parent)
            restoreSingle(strategy, metadata, request);

            logService.success(log, metadata.getStoragePath());

            return new RestoreResponse(
                    "SUCCESS",
                    request.getBackupId(),
                    log.getId(),
                    "Restore completed successfully"
            );

        } catch (Exception ex) {
            logService.fail(log, ex.getMessage());
            throw new BackupException("Restore failed: " + ex.getMessage(), ex);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Restores a single backup file (handles decompression automatically).
     * Uses storage strategy to resolve path (local file or S3 download).
     */
    private void restoreSingle(DatabaseBackupStrategy strategy,
                               BackupMetadata metadata,
                               RestoreRequest request) throws Exception {

        StorageStrategy storageStrategy = storageFactory.getStrategy(metadata.getStorageType());
        File backupFile = storageStrategy.resolveToLocalFile(metadata.getStoragePath());

        if (!backupFile.exists()) {
            throw new BackupException(
                    "Backup file not found: " + metadata.getStoragePath());
        }

        File dumpFile = backupFile;

        try {
            // Decompress if needed (.gz)
            if (metadata.isCompressed()) {
                dumpFile = strategy.decompressBackup(backupFile);
            }

            // Run the restore
            strategy.restoreBackup(dumpFile, request);
        } finally {
            // Clean up temp decompressed file if we created it
            if (metadata.isCompressed() && dumpFile != backupFile && dumpFile.exists()) {
                dumpFile.delete();
            }
            // For S3 (and other cloud), backupFile is a temp download — delete it
            if (metadata.getStorageType() != StorageType.LOCAL && backupFile.exists()) {
                backupFile.delete();
            }
        }
    }

    /**
     * If the caller didn't provide target credentials, fall back to the original
     * DB info stored in the metadata. Useful when restoring to the same server.
     *
     * Note: password is NOT stored in metadata for security reasons.
     * The caller MUST provide at least targetPassword.
     */
    private void resolveTargetCredentials(RestoreRequest request,
                                          BackupMetadata metadata) {
        if (request.getTargetHost() == null || request.getTargetHost().isBlank()) {
            throw new BackupException(
                    "targetHost is required. Provide the host to restore into.");
        }
        if (request.getTargetPort() == null) {
            throw new BackupException("targetPort is required.");
        }
        if (request.getTargetUsername() == null || request.getTargetUsername().isBlank()) {
            throw new BackupException("targetUsername is required.");
        }
        if (request.getTargetPassword() == null || request.getTargetPassword().isBlank()) {
            throw new BackupException(
                    "targetPassword is required. Passwords are not stored in metadata.");
        }

        // If no target DB name provided, restore into the original DB name
        if (request.getTargetDbName() == null || request.getTargetDbName().isBlank()) {
            request.setTargetDbName(metadata.getDbName());
        }
    }
}