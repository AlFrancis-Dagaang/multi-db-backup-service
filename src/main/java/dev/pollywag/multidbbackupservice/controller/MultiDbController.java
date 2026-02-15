package dev.pollywag.multidbbackupservice.controller;

import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.response.BackupResponse;
import dev.pollywag.multidbbackupservice.service.BackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MultiDbController {
    private final BackupService backupService;
    public MultiDbController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/backup")
    public ResponseEntity<BackupResponse> runBackup(@RequestBody BackupRequest request) {
        BackupResponse response = backupService.backup(request);
        return ResponseEntity.ok(response);
    }
}
