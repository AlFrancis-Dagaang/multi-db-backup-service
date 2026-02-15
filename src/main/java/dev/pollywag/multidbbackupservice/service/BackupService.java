package dev.pollywag.multidbbackupservice.service;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.factory.DatabaseStrategyFactory;
import dev.pollywag.multidbbackupservice.factory.StorageStrategyFactory;
import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import dev.pollywag.multidbbackupservice.model.entity.BackupMetadata;
import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.response.BackupResponse;
import dev.pollywag.multidbbackupservice.repository.BackupMetadataRepository;
import dev.pollywag.multidbbackupservice.strategy.database.DatabaseBackupStrategy;
import dev.pollywag.multidbbackupservice.strategy.storage.StorageStrategy;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.List;


@Service
public class BackupService {

    private final DatabaseStrategyFactory dbFactory;
    private final StorageStrategyFactory storageFactory;
    private final LogService logService;
    // final NotificationService notificationService;
    private final BackupMetadataRepository metadataRepository;

    public BackupService(DatabaseStrategyFactory dbFactory,
                         StorageStrategyFactory storageFactory,
                         LogService logService,
                         BackupMetadataRepository metadataRepository
                         ) {
        this.dbFactory = dbFactory;
        this.storageFactory = storageFactory;
        this.logService = logService;
        //this.notificationService = notificationService;
        this.metadataRepository = metadataRepository;
    }
    public BackupResponse backup(BackupRequest request) {
        // Start log
        BackupLog log = logService.start("BACKUP", request.getDbName());
        log.setDbType(request.getDbType());
        log.setStorageType(request.getStorageType());

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String backupType = request.getBackupType().name().toLowerCase();
        String tempFileName = request.getDbName()
                + "_"
                + backupType
                + "_"
                + timestamp
                + ".sql";
        String tempFilePath = System.getProperty("java.io.tmpdir")
                + java.io.File.separator
                + tempFileName;

        try {
            DatabaseBackupStrategy dbStrategy =
                    dbFactory.getStrategy(request.getDbType());
            // TEST CONNECTION FIRST
            boolean connected = dbStrategy.testConnection(request);

            if (!connected) {
                throw new BackupException("Database connection failed. Invalid credentials or host.");
            }

            File backupFile;

            // FULL vs INCREMENTAL
            if (request.getBackupType() == BackupType.FULL) {
                backupFile = dbStrategy.performBackup(request, tempFilePath);
            } else {
                backupFile = dbStrategy.performIncrementalBackup(request, tempFilePath);
            }

            // COMPRESS
            if (request.isCompress()) {
                backupFile = dbStrategy.compressBackup(backupFile);
            }

            // STORE
            StorageStrategy storage =
                    storageFactory.getStrategy(request.getStorageType());

            String finalLocation = storage.store(backupFile, request);

            File storedBackupFile = new File(finalLocation);

            // SUCCESS LOG
            logService.success(log, finalLocation);

            saveMetaData(request, storedBackupFile, finalLocation, log );

            return new BackupResponse(
                    "SUCCESS",
                    finalLocation,
                    log.getId(),
                    "Backup completed successfully"
            );
        } catch (Exception ex) {

            logService.fail(log, ex.getMessage());

            throw new BackupException(
                    "Backup failed: " + ex.getMessage(),
                    ex
            );
        }
    }

    private void saveMetaData(BackupRequest request,
                                      File backupFile,
                                      String storagePath,
                                      BackupLog log) throws IOException {

        BackupMetadata metadata = new BackupMetadata();

        metadata.setBackupId(log.getId());
        metadata.setDbName(request.getDbName());
        metadata.setDbType(request.getDbType());
        metadata.setBackupType(request.getBackupType());
        metadata.setCompressed(request.isCompress());
        metadata.setFileName(backupFile.getName());
        metadata.setFileSizeBytes(backupFile.length());
        metadata.setStorageType(request.getStorageType());
        metadata.setStoragePath(storagePath);
        metadata.setStartTime(log.getStartTime());
        metadata.setEndTime(log.getEndTime());
        metadata.setStatus(log.getStatus());

        if (request.getBackupType() == BackupType.INCREMENTAL) {
            // Later you can improve this with parent lookup
            metadata.setParentBackupId(findLatestFullBackupId(request));
        }

        metadataRepository.save(metadata);
    }

    private String findLatestFullBackupId(BackupRequest request) {
        // Option 1: Using the custom query
        List<BackupMetadata> fullBackups = metadataRepository.findLatestFullBackup(
                request.getDbName(),
                request.getDbType()
        );

        if (fullBackups.isEmpty()) {
            throw new RuntimeException(
                    "No full backup found for " + request.getDbName() +
                            ". Cannot create incremental backup without a base full backup."
            );
        }

        return fullBackups.get(0).getBackupId();

    }




}
