package dev.pollywag.multidbbackupservice.service;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.factory.DatabaseStrategyFactory;
import dev.pollywag.multidbbackupservice.factory.StorageStrategyFactory;
import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.response.BackupResponse;
import dev.pollywag.multidbbackupservice.strategy.database.DatabaseBackupStrategy;
import dev.pollywag.multidbbackupservice.strategy.storage.StorageStrategy;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class BackupService {

    private final DatabaseStrategyFactory dbFactory;
    private final StorageStrategyFactory storageFactory;
    private final LogService logService;
    // final NotificationService notificationService;

    public BackupService(DatabaseStrategyFactory dbFactory,
                         StorageStrategyFactory storageFactory,
                         LogService logService
                         ) {
        this.dbFactory = dbFactory;
        this.storageFactory = storageFactory;
        this.logService = logService;
        //this.notificationService = notificationService;
    }
    public BackupResponse backup(BackupRequest request) {

        // Start log
        BackupLog log = logService.start("BACKUP", request.getDbName());
        log.setDbType(request.getDbType());
        log.setStorageType(request.getStorageType());

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String tempFileName = request.getDbName() + "_" + timestamp + ".sql";
        String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + tempFileName;

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

            // METADATA
            File metadataFile =
                    generateMetadataJson(request, storedBackupFile, finalLocation, log);

            String metadataStoredPath = storage.store(metadataFile, request);

            return new BackupResponse(
                    "SUCCESS",
                    finalLocation,
                    metadataStoredPath,
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



    public boolean testConnection (BackupRequest request) throws BackupException {
        DatabaseBackupStrategy dbStrategy = dbFactory.getStrategy(request.getDbType());
        System.out.println(request.getDbType());
        return dbStrategy.testConnection(request);
    }


    private File generateMetadataJson(BackupRequest request,
                                      File backupFile,
                                      String storagePath,
                                      BackupLog log) throws IOException {

        // Use the same folder as backup file to create metadata
        String jsonFileName = backupFile.getName().replaceAll("\\.sql(\\.gz)?$", ".json");
        File jsonFile = new File(backupFile.getParentFile(), jsonFileName);

        // Build metadata map
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("backupId", log.getId());
        metadata.put("dbType", request.getDbType());
        metadata.put("dbName", request.getDbName());
        metadata.put("backupType", request.getBackupType());
        metadata.put("tables", request.getTables());
        metadata.put("compressed", request.isCompress());
        metadata.put("fileName", backupFile.getName());
        metadata.put("fileSizeBytes", backupFile.length());
        metadata.put("storageType", request.getStorageType());
        metadata.put("storagePath", storagePath);
        metadata.put("startTime", log.getStartTime());
        metadata.put("endTime", log.getEndTime());
        metadata.put("status", log.getStatus());
        metadata.put("errorMessage", log.getError());

        // Write JSON using Jackson
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, metadata);

        return jsonFile;
    }


}
