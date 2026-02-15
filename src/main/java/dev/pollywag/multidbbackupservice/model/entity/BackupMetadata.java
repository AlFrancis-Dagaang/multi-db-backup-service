package dev.pollywag.multidbbackupservice.model.entity;

import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_metadata")
@Data
public class BackupMetadata {

    @Id
    private String backupId;

    private String dbName;

    @Enumerated(EnumType.STRING)
    private DatabaseType dbType;

    @Enumerated(EnumType.STRING)
    private BackupType backupType;

    private boolean compressed;

    private String fileName;

    private long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    private String storagePath;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    // 🔥 Optional but recommended for incremental
    private String parentBackupId;
}