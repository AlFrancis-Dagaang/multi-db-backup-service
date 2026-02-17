package dev.pollywag.multidbbackupservice.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestoreRequest {

    // The backupId from BackupMetadata — client sends only this
    private String backupId;

    // Optional: override the target DB credentials
    // If null, the service will use the stored metadata's DB info
    private String targetHost;
    private Integer targetPort;
    private String targetUsername;
    private String targetPassword;
    private String targetDbName; // optional: restore into a different db name
}