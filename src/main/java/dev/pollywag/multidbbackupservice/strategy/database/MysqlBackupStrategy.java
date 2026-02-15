package dev.pollywag.multidbbackupservice.strategy.database;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;


@Component
public class MysqlBackupStrategy implements DatabaseBackupStrategy{

    @Override
    public boolean testConnection(BackupRequest request) {
        String url = "jdbc:mysql://" + request.getHost() + ":" + request.getPort() + "/" + request.getDbName();
        try (Connection conn = DriverManager.getConnection(url, request.getUsername(), request.getPassword())) {
            return true;
        } catch (Exception e) {
            throw new BackupException("MySQL connection failed", e);
        }
    }

    @Override
    public File performBackup(BackupRequest request, String tempFilePath) {
        try {
            File outputFile = new File(tempFilePath);

            // ensure parent directory exists
            outputFile.getParentFile().mkdirs();

            // build mysqldump command
            List<String> command = new ArrayList<>();

            command.add("mysqldump");
            command.add("-h");
            command.add(request.getHost());
            command.add("-P");
            command.add(String.valueOf(request.getPort()));
            command.add("-u");
            command.add(request.getUsername());
            command.add("-p" + request.getPassword());
            command.add(request.getDbName());

            ProcessBuilder pb = new ProcessBuilder(command);

            // merge error stream
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // write dump to file with compression
            try (
                    InputStream inputStream = process.getInputStream();
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    GZIPOutputStream gzipOut = new GZIPOutputStream(fos)
            ) {

                byte[] buffer = new byte[8192];
                int length;

                while ((length = inputStream.read(buffer)) != -1) {
                    gzipOut.write(buffer, 0, length);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new BackupException("mysqldump failed with exit code " + exitCode);
            }

            return outputFile;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackupException("MySQL backup failed", e);
        }
    }

    @Override
    public File performIncrementalBackup(BackupRequest request,
                                         String outputPath) throws Exception {

        if (request.getTables() == null ||
                request.getTables().isEmpty()) {
            throw new BackupException(
                    "Incremental backup requires selected tables."
            );
        }

        // Check if tables exist in the database
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + request.getHost() + ":" + request.getPort() + "/" + request.getDbName(),
                request.getUsername(),
                request.getPassword())) {

            DatabaseMetaData metaData = conn.getMetaData();
            List<String> nonExistentTables = new ArrayList<>();

            for (String table : request.getTables()) {
                try (ResultSet rs = metaData.getTables(null, null, table, new String[]{"TABLE"})) {
                    if (!rs.next()) {
                        nonExistentTables.add(table);
                    }
                }
            }

            if (!nonExistentTables.isEmpty()) {
                throw new BackupException(
                        "The following tables do not exist in the database: " + nonExistentTables
                );
            }
        }


        List<String> command = new ArrayList<>();

        command.add("mysqldump");
        command.add("-h");
        command.add(request.getHost());

        command.add("-P");
        command.add(String.valueOf(request.getPort()));

        command.add("-u");
        command.add(request.getUsername());

        command.add("-p" + request.getPassword());

        command.add(request.getDbName());

        // add selected tables
        command.addAll(request.getTables());

        ProcessBuilder pb = new ProcessBuilder(command);

        pb.redirectOutput(new File(outputPath));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new BackupException(
                    "Incremental backup failed. mysqldump exit code: " + exitCode
            );
        }

        return new File(outputPath);
    }

    @Override
    public File compressBackup(File inputFile) throws Exception {

        String gzipFilePath = inputFile.getAbsolutePath() + ".gz";

        try (
                FileInputStream fis = new FileInputStream(inputFile);
                FileOutputStream fos = new FileOutputStream(gzipFilePath);
                java.util.zip.GZIPOutputStream gzipOS =
                        new java.util.zip.GZIPOutputStream(fos)
        ) {

            byte[] buffer = new byte[1024];
            int len;

            while ((len = fis.read(buffer)) > 0) {
                gzipOS.write(buffer, 0, len);
            }
        }

        // optional: delete original sql file
        inputFile.delete();

        return new File(gzipFilePath);
    }





}
