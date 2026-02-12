package dev.pollywag.multidbbackupservice.strategy.storage;

import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${backup.local.default-path}")
    private String defaultBackupPath;

    @Override
    public String store(File file, BackupRequest request) {
        try {
            // Determine base directory
            String baseDir = (request.getLocalPath() != null && !request.getLocalPath().isBlank())
                    ? request.getLocalPath()
                    : defaultBackupPath;

            // Add DB type subfolder
            String dbFolder = getFolderByDbType(request.getDbType());
            Path dir = Paths.get(baseDir, dbFolder);

            // Create directories if not exist
            if (!Files.exists(dir)) Files.createDirectories(dir);

            // Destination path
            Path dest = dir.resolve(file.getName());

            // Move file
            Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            return dest.toString();

        } catch (Exception e) {
            // You can throw a custom exception instead of returning ""
            return "";
        }
    }

    private String getFolderByDbType(DatabaseType dbType) {
        switch (dbType) {
            case MYSQL:
                return "mysql";
            case POSTGRESQL:
                return "postgresql";
            case MONGODB:
                return "mongodb";
            default:
                return "other";
        }
    }
}
