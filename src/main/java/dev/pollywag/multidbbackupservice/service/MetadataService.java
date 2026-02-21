package dev.pollywag.multidbbackupservice.service;

import dev.pollywag.multidbbackupservice.model.entity.BackupMetadata;
import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import dev.pollywag.multidbbackupservice.repository.BackupMetadataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MetadataService {

    private final BackupMetadataRepository metadataRepository;

    public MetadataService(BackupMetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public List<BackupMetadata> getAllMetadata() {
        return metadataRepository.findAll();
    }

    public BackupMetadata getMetadataById(String backupId) {
        return metadataRepository.findById(backupId).orElse(null);
    }

    public List<BackupMetadata> getFilteredMetadata(
            String dbName,
            String status,
            DatabaseType dbType,
            BackupType backupType,
            StorageType storageType,
            Boolean compressed,
            String parentBackupId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return metadataRepository.findWithFilters(
                dbName, status, dbType, backupType, storageType, compressed, parentBackupId, startDate, endDate);
    }

    public List<BackupMetadata> getMetadataByDatabase(String dbName) {
        return metadataRepository.findByDbName(dbName);
    }

    public List<BackupMetadata> getMetadataByStatus(String status) {
        return metadataRepository.findByStatus(status);
    }

    public List<BackupMetadata> getMetadataByBackupType(BackupType backupType) {
        return metadataRepository.findByBackupType(backupType);
    }

    public List<BackupMetadata> getMetadataByStorageType(StorageType storageType) {
        return metadataRepository.findByStorageType(storageType);
    }

    public List<BackupMetadata> getMetadataByCompressed(boolean compressed) {
        return metadataRepository.findByCompressed(compressed);
    }

    public List<BackupMetadata> getMetadataByParentBackupId(String parentBackupId) {
        return metadataRepository.findByParentBackupId(parentBackupId);
    }

    public List<BackupMetadata> getMetadataByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return metadataRepository.findByStartTimeBetween(startDate, endDate);
    }
}
