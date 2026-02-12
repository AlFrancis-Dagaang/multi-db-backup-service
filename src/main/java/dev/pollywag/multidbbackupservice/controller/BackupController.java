package dev.pollywag.multidbbackupservice.controller;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.response.BackupResponse;
import dev.pollywag.multidbbackupservice.service.BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backup/*")
public class BackupController {
    private final BackupService backupService;
    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/run")
    public ResponseEntity<BackupResponse> runBackup(@RequestBody BackupRequest request) {
        BackupResponse response = backupService.backup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-connection")
    public ResponseEntity<String> testConnection(@RequestBody BackupRequest request) throws BackupException {
        if(backupService.testConnection(request)) {
            return ResponseEntity.ok("Success");
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
