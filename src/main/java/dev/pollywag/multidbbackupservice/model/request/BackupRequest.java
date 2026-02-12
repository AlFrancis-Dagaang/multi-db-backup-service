package dev.pollywag.multidbbackupservice.model.request;

import dev.pollywag.multidbbackupservice.model.enums.BackupType;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.enums.StorageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupRequest {
    private DatabaseType dbType;
    private String host;
    private int port;
    private String username;
    private String password;
    private String dbName;

    private BackupType backupType;//full or incremental(specific tables)
    private List<String> tables;
    private boolean compress;

    private StorageType storageType;
    private String localPath;  // optional override
    private String cloudBucket; // optional AWS/Azure/GCP bucket
    private String cloudFolder; // optional folder

    private String notifyEmail;
}

