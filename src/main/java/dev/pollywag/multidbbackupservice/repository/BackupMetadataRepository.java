package dev.pollywag.multidbbackupservice.repository;

import dev.pollywag.multidbbackupservice.model.entity.BackupMetadata;
import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BackupMetadataRepository
        extends JpaRepository<BackupMetadata, String> {

    List<BackupMetadata> findByDbNameAndDbTypeOrderByStartTimeAsc(String dbName, DatabaseType dbType);

    @Query("SELECT b FROM BackupMetadata b WHERE b.dbName = :dbName " +
            "AND b.dbType = :dbType " +
            "AND b.backupType = 'FULL' " +
            "AND b.status = 'SUCCESS' " +
            "ORDER BY b.endTime DESC")
    List<BackupMetadata> findLatestFullBackup(
            @Param("dbName") String dbName,
            @Param("dbType") DatabaseType dbType
    );

    List<BackupMetadata> findByDbName(String dbName);
    List<BackupMetadata> findByStatus(String status);
    List<BackupMetadata> findByBackupType(BackupType backupType);
    List<BackupMetadata> findByStorageType(StorageType storageType);
    List<BackupMetadata> findByCompressed(boolean compressed);
    List<BackupMetadata> findByParentBackupId(String parentBackupId);
    List<BackupMetadata> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT b FROM BackupMetadata b WHERE " +
            "(:dbName IS NULL OR b.dbName = :dbName) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:dbType IS NULL OR b.dbType = :dbType) AND " +
            "(:backupType IS NULL OR b.backupType = :backupType) AND " +
            "(:storageType IS NULL OR b.storageType = :storageType) AND " +
            "(:compressed IS NULL OR b.compressed = :compressed) AND " +
            "(:parentBackupId IS NULL OR b.parentBackupId = :parentBackupId) AND " +
            "(:startDate IS NULL OR b.startTime >= :startDate) AND " +
            "(:endDate IS NULL OR b.startTime <= :endDate)")
    List<BackupMetadata> findWithFilters(
            @Param("dbName") String dbName,
            @Param("status") String status,
            @Param("dbType") DatabaseType dbType,
            @Param("backupType") BackupType backupType,
            @Param("storageType") StorageType storageType,
            @Param("compressed") Boolean compressed,
            @Param("parentBackupId") String parentBackupId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
