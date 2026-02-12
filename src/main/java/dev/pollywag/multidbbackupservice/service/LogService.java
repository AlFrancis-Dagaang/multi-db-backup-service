package dev.pollywag.multidbbackupservice.service;

import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;  // ← Jackson's TypeReference
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final List<BackupLog> logs = new ArrayList<>();

    private final File logFile = new File("logs/backup-logs.json");

    private final ObjectMapper mapper = new ObjectMapper();

    public LogService() {
        loadLogsFromFile();
    }

    public BackupLog start(String action, String dbName) {

        BackupLog log = new BackupLog();
        log.setId(UUID.randomUUID().toString());
        log.setAction(action);
        log.setDbName(dbName);
        log.setStatus("STARTED");
        log.setStartTime(LocalDateTime.now());

        logs.add(log);
        saveLogsToFile();

        return log;
    }

    public void success(BackupLog log, String filePath) {

        log.setStatus("SUCCESS");
        log.setFilePath(filePath);
        log.setEndTime(LocalDateTime.now());

        saveLogsToFile();
    }

    public void fail(BackupLog log, String error) {

        log.setStatus("FAILED");
        log.setError(error);
        log.setEndTime(LocalDateTime.now());

        saveLogsToFile();
    }

    public List<BackupLog> getAllLogs() {
        return logs;
    }

    public List<BackupLog> getLogsByDbType(String dbType) {

        return logs.stream()
                .filter(l ->
                        l.getDbType() != null &&
                                l.getDbType().name().equalsIgnoreCase(dbType))
                .collect(Collectors.toList());
    }

    public BackupLog getLogById(String id) {

        return logs.stream()
                .filter(l -> l.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void saveLogsToFile() {

        try {

            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(logFile, logs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLogsFromFile() {

        try {

            if (logFile.exists()) {

                List<BackupLog> loadedLogs =
                        mapper.readValue(
                                logFile,
                                new TypeReference<List<BackupLog>>() {}
                        );

                logs.addAll(loadedLogs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

