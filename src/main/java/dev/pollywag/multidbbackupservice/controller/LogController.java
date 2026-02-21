package dev.pollywag.multidbbackupservice.controller;

import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/logs")
public class LogController {
    
    private final LogService logService;
    
    public LogController(LogService logService) {
        this.logService = logService;
    }
    
    /**
     * Get all logs with optional filtering
     * GET /api/logs?action=BACKUP&status=SUCCESS&dbName=mydb&dbType=MYSQL&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping
    public ResponseEntity<List<BackupLog>> getAllLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dbName,
            @RequestParam(required = false) DatabaseType dbType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("Fetching logs with filters - action: {}, status: {}, dbName: {}, dbType: {}, startDate: {}, endDate: {}", 
                action, status, dbName, dbType, startDate, endDate);
        
        List<BackupLog> logs = logService.getFilteredLogs(action, status, dbName, dbType, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get a specific log by ID
     * GET /api/logs/{logId}
     */
    @GetMapping("/{logId}")
    public ResponseEntity<BackupLog> getLogById(@PathVariable String logId) {
        log.debug("Fetching backup log with id: {}", logId);
        BackupLog backupLog = logService.getLogById(logId);
        
        if (backupLog == null) {
            log.warn("Backup log not found with id: {}", logId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(backupLog);
    }
    
    /**
     * Get logs by database name
     * GET /api/logs/database/{dbName}
     */
    @GetMapping("/database/{dbName}")
    public ResponseEntity<List<BackupLog>> getLogsByDatabase(@PathVariable String dbName) {
        log.debug("Fetching logs for database: {}", dbName);
        List<BackupLog> logs = logService.getLogsByDatabase(dbName);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get logs by action type (BACKUP or RESTORE)
     * GET /api/logs/action/{action}
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<List<BackupLog>> getLogsByAction(@PathVariable String action) {
        log.debug("Fetching logs for action: {}", action);
        List<BackupLog> logs = logService.getLogsByAction(action);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get logs by status (SUCCESS, FAILED, STARTED)
     * GET /api/logs/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BackupLog>> getLogsByStatus(@PathVariable String status) {
        log.debug("Fetching logs with status: {}", status);
        List<BackupLog> logs = logService.getLogsByStatus(status);
        return ResponseEntity.ok(logs);
    }
}