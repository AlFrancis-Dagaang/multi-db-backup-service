package dev.pollywag.multidbbackupservice.repository;

import dev.pollywag.multidbbackupservice.model.entity.BackupMetadata;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupMetadataRepository
        extends JpaRepository<BackupMetadata, String> {

    List<BackupMetadata>
    findByDbNameAndDbTypeOrderByStartTimeAsc(String dbName, DatabaseType dbType);

    @Query("SELECT b FROM BackupMetadata b WHERE b.dbName = :dbName " +
            "AND b.dbType = :dbType " +
            "AND b.backupType = 'FULL' " +
            "AND b.status = 'SUCCESS' " +
            "ORDER BY b.endTime DESC")
    List<BackupMetadata> findLatestFullBackup(
            @Param("dbName") String dbName,
            @Param("dbType") DatabaseType dbType
    );




}
