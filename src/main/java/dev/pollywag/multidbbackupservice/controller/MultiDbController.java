package dev.pollywag.multidbbackupservice.controller;

import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.request.RestoreRequest;
import dev.pollywag.multidbbackupservice.model.response.BackupResponse;
import dev.pollywag.multidbbackupservice.model.response.RestoreResponse;
import dev.pollywag.multidbbackupservice.service.BackupService;
import dev.pollywag.multidbbackupservice.service.RestoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MultiDbController {
    private final BackupService backupService;
    private final RestoreService restoreService;

    public MultiDbController(BackupService backupService,  RestoreService restoreService) {
        this.backupService = backupService;
        this.restoreService = restoreService;
    }

    @PostMapping("/backup")
    public ResponseEntity<BackupResponse> runBackup(@RequestBody BackupRequest request) {
        BackupResponse response = backupService.backup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/restore")
    public ResponseEntity<RestoreResponse> runRestore(@RequestBody RestoreRequest request) {
        RestoreResponse response = restoreService.restore(request);
        return ResponseEntity.ok(response);
    }


}
