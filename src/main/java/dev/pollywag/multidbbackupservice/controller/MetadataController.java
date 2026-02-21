package dev.pollywag.multidbbackupservice.controller;

import dev.pollywag.multidbbackupservice.model.entity.BackupMetadata;
import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import dev.pollywag.multidbbackupservice.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Get all backup metadata with optional filtering
     * GET /api/metadata?dbName=...&status=...&dbType=...&backupType=...&storageType=...&compressed=...&parentBackupId=...&startDate=...&endDate=...
     */
    @GetMapping
    public ResponseEntity<List<BackupMetadata>> getAllMetadata(
            @RequestParam(required = false) String dbName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) DatabaseType dbType,
            @RequestParam(required = false) BackupType backupType,
            @RequestParam(required = false) StorageType storageType,
            @RequestParam(required = false) Boolean compressed,
            @RequestParam(required = false) String parentBackupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("Fetching metadata with filters - dbName: {}, status: {}, dbType: {}, backupType: {}, storageType: {}, compressed: {}, parentBackupId: {}, startDate: {}, endDate: {}",
                dbName, status, dbType, backupType, storageType, compressed, parentBackupId, startDate, endDate);

        List<BackupMetadata> metadata = metadataService.getFilteredMetadata(
                dbName, status, dbType, backupType, storageType, compressed, parentBackupId, startDate, endDate);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Get backup metadata by ID
     * GET /api/metadata/{backupId}
     */
    @GetMapping("/{backupId}")
    public ResponseEntity<BackupMetadata> getMetadataById(@PathVariable String backupId) {
        log.debug("Fetching backup metadata with id: {}", backupId);
        BackupMetadata metadata = metadataService.getMetadataById(backupId);

        if (metadata == null) {
            log.warn("Backup metadata not found with id: {}", backupId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(metadata);
    }

    /**
     * Get metadata by database name
     * GET /api/metadata/database/{dbName}
     */
    @GetMapping("/database/{dbName}")
    public ResponseEntity<List<BackupMetadata>> getMetadataByDatabase(@PathVariable String dbName) {
        log.debug("Fetching metadata for database: {}", dbName);
        List<BackupMetadata> metadata = metadataService.getMetadataByDatabase(dbName);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Get metadata by status (e.g. SUCCESS, FAILED)
     * GET /api/metadata/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BackupMetadata>> getMetadataByStatus(@PathVariable String status) {
        log.debug("Fetching metadata with status: {}", status);
        List<BackupMetadata> metadata = metadataService.getMetadataByStatus(status);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Get metadata by backup type (FULL or INCREMENTAL)
     * GET /api/metadata/backup-type/{backupType}
     */
    @GetMapping("/backup-type/{backupType}")
    public ResponseEntity<List<BackupMetadata>> getMetadataByBackupType(@PathVariable BackupType backupType) {
        log.debug("Fetching metadata for backup type: {}", backupType);
        List<BackupMetadata> metadata = metadataService.getMetadataByBackupType(backupType);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Get metadata by storage type (LOCAL, AWS_S3, AZURE_BLOB, GCP_STORAGE)
     * GET /api/metadata/storage-type/{storageType}
     */
    @GetMapping("/storage-type/{storageType}")
    public ResponseEntity<List<BackupMetadata>> getMetadataByStorageType(@PathVariable StorageType storageType) {
        log.debug("Fetching metadata for storage type: {}", storageType);
        List<BackupMetadata> metadata = metadataService.getMetadataByStorageType(storageType);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Get metadata by compressed flag
     * GET /api/metadata/compressed/{compressed}
     */
    @GetMapping("/compressed/{compressed}")
    public ResponseEntity<List<BackupMetadata>> getMetadataByCompressed(@PathVariable boolean compressed) {
        log.debug("Fetching metadata with compressed: {}", compressed);
        List<BackupMetadata> metadata = metadataService.getMetadataByCompressed(compressed);
        return ResponseEntity.ok(metadata);
    }

    /**
     * Get metadata by parent backup ID (for incremental backups)
     * GET /api/metadata/parent/{parentBackupId}
     */
    @GetMapping("/parent/{parentBackupId}")
    public ResponseEntity<List<BackupMetadata>> getMetadataByParentBackupId(@PathVariable String parentBackupId) {
        log.debug("Fetching metadata for parent backup id: {}", parentBackupId);
        List<BackupMetadata> metadata = metadataService.getMetadataByParentBackupId(parentBackupId);
        return ResponseEntity.ok(metadata);
    }
}
