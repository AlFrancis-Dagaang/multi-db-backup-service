package dev.pollywag.multidbbackupservice.controller;

import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.request.RestoreRequest;
import dev.pollywag.multidbbackupservice.model.response.BackupResponse;
import dev.pollywag.multidbbackupservice.model.response.RestoreResponse;
import dev.pollywag.multidbbackupservice.service.BackupService;
import dev.pollywag.multidbbackupservice.service.LogService;
import dev.pollywag.multidbbackupservice.service.RestoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MultiDbController {
    private final BackupService backupService;
    private final RestoreService restoreService;
    private final LogService logService;

    public MultiDbController(BackupService backupService, RestoreService restoreService, LogService logService) {
        this.backupService = backupService;
        this.restoreService = restoreService;
        this.logService = logService;
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

    @GetMapping("/logs")
    public ResponseEntity<List<BackupLog>> getAllBackups() {
        List<BackupLog> logs = logService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<BackupLog> getLogById(@PathVariable("id") String id) {
        System.out.println(id);
        BackupLog log = logService.getLogById(id);
        return ResponseEntity.ok(log);
    }
}
