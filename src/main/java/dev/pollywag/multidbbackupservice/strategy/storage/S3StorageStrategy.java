package dev.pollywag.multidbbackupservice.strategy.storage;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class S3StorageStrategy implements StorageStrategy {

    private static final String S3_PREFIX = "s3://";

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.default-bucket:}")
    private String defaultBucket;

    @Value("${aws.s3.default-prefix:backups}")
    private String defaultPrefix;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    @Override
    public String store(File file, BackupRequest request) {
        String bucket = (request.getCloudBucket() != null && !request.getCloudBucket().isBlank())
                ? request.getCloudBucket()
                : defaultBucket;
        if (bucket == null || bucket.isBlank()) {
            throw new BackupException("S3 bucket not configured. Set aws.s3.default-bucket or provide cloudBucket in request.");
        }

        String key = buildKey(request, file.getName());

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.putObject(putRequest, RequestBody.fromFile(file));
            return S3_PREFIX + bucket + "/" + key;
        } catch (S3Exception e) {
            throw new BackupException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public File resolveToLocalFile(String storagePath) {
        if (storagePath == null || !storagePath.startsWith(S3_PREFIX)) {
            throw new BackupException("Invalid S3 storage path: " + storagePath);
        }
        String withoutPrefix = storagePath.substring(S3_PREFIX.length());
        int firstSlash = withoutPrefix.indexOf('/');
        if (firstSlash <= 0) {
            throw new BackupException("Invalid S3 storage path: " + storagePath);
        }
        String bucket = withoutPrefix.substring(0, firstSlash);
        String key = withoutPrefix.substring(firstSlash + 1);

        try {
            String suffix = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
            Path tempFile = Files.createTempFile("s3-backup-", "-" + suffix);
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.getObject(getRequest, ResponseTransformer.toFile(tempFile));
            return tempFile.toFile();
        } catch (IOException e) {
            throw new BackupException("Failed to download from S3: " + e.getMessage(), e);
        }
    }

    private String buildKey(BackupRequest request, String fileName) {
        String prefix = (request.getCloudFolder() != null && !request.getCloudFolder().isBlank())
                ? request.getCloudFolder()
                : defaultPrefix;
        String dbFolder = getFolderByDbType(request.getDbType());
        String dbName = request.getDbName() != null ? request.getDbName() : "default";
        return prefix + "/" + dbFolder + "/" + dbName + "/" + fileName;
    }

    private String getFolderByDbType(DatabaseType dbType) {
        return switch (dbType) {
            case MYSQL -> "mysql";
            case POSTGRESQL -> "postgresql";
            case MONGODB -> "mongodb";
            default -> "other";
        };
    }
}
