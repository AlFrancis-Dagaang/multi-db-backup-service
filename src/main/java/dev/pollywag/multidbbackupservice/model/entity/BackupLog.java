package dev.pollywag.multidbbackupservice.model.entity;

import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_logs")
@Data
public class BackupLog {

    @Id
    private String id;

    private String action;

    @Enumerated(EnumType.STRING)
    private DatabaseType dbType;

    private String dbName;

    private String status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    private String filePath;

    @Column(length = 2000)
    private String error;
}
