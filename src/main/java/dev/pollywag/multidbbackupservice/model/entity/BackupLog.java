package dev.pollywag.multidbbackupservice.model.entity;

import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BackupLog {
    private String id;
    private String action; // BACKUP, RESTORE, CONNECTION_TEST
    private DatabaseType dbType;
    private String dbName;

    private String status; // STARTED, SUCCESS, FAILED
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private StorageType storageType;
    private String filePath;
    private String error;
}
