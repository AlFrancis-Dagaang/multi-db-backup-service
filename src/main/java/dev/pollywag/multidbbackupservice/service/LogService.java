package dev.pollywag.multidbbackupservice.service;

import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.repository.BackupLogRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LogService {

    private final BackupLogRepository logRepository;

    public LogService(BackupLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public BackupLog start(String action, String dbName) {
        BackupLog log = new BackupLog();
        log.setId(UUID.randomUUID().toString());
        log.setAction(action);
        log.setDbName(dbName);
        log.setStatus("STARTED");
        log.setStartTime(LocalDateTime.now());
        return logRepository.save(log);
    }

    public void success(BackupLog log, String filePath) {
        log.setStatus("SUCCESS");
        log.setFilePath(filePath);
        log.setEndTime(LocalDateTime.now());
        logRepository.save(log);
    }

    public void fail(BackupLog log, String error) {
        log.setStatus("FAILED");
        log.setError(error);
        log.setEndTime(LocalDateTime.now());
        logRepository.save(log);
    }

    public List<BackupLog> getAllLogs() {
        return logRepository.findAll();
    }

    public BackupLog getLogById(String id) {
        return logRepository.findById(id).orElse(null);
    }
    
    // New filtering methods
    
    public List<BackupLog> getFilteredLogs(String action, String status, String dbName, 
                                           DatabaseType dbType, LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.findWithFilters(action, status, dbName, dbType, startDate, endDate);
    }
    
    public List<BackupLog> getLogsByAction(String action) {
        return logRepository.findByAction(action);
    }
    
    public List<BackupLog> getLogsByStatus(String status) {
        return logRepository.findByStatus(status);
    }
    
    public List<BackupLog> getLogsByDatabase(String dbName) {
        return logRepository.findByDbName(dbName);
    }
    
    public List<BackupLog> getLogsByDatabaseType(DatabaseType dbType) {
        return logRepository.findByDbType(dbType);
    }
}