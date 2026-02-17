package dev.pollywag.multidbbackupservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestoreResponse {

    private String status;        // SUCCESS or FAILED
    private String backupId;      // which backup was restored
    private String logId;         // restore log ID
    private String message;       // human-readable result
}