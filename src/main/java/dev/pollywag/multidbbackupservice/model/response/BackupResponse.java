package dev.pollywag.multidbbackupservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupResponse {
    private String status;
    private String fileLocation;
    private String metadataLocation;
    private String message;
}
