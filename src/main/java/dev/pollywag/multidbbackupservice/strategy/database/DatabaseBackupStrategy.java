package dev.pollywag.multidbbackupservice.strategy.database;

import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.request.RestoreRequest;

import java.io.File;

public interface DatabaseBackupStrategy {
    boolean testConnection(BackupRequest request);
    File performBackup(BackupRequest request, String tempFilePath);
    //void restoreDatabase(RestoreRequest request);
    File performIncrementalBackup(BackupRequest request, String outputPath) throws Exception;

    File compressBackup(File backupFile) throws Exception;

    File decompressBackup(File compressedFile) throws Exception;

    void restoreBackup(File dumpFile, RestoreRequest request) throws Exception;
}
