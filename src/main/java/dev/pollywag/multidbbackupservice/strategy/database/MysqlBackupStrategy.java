package dev.pollywag.multidbbackupservice.strategy.database;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.model.request.BackupRequest;
import dev.pollywag.multidbbackupservice.model.request.RestoreRequest;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


@Component
public class MysqlBackupStrategy implements DatabaseBackupStrategy {

    // ─── Connection test ───────────────────────────────────────────────────────

    @Override
    public boolean testConnection(BackupRequest request) {
        String url = "jdbc:mysql://" + request.getHost() + ":" + request.getPort() + "/" + request.getDbName();
        try (Connection conn = DriverManager.getConnection(url, request.getUsername(), request.getPassword())) {
            return true;
        } catch (Exception e) {
            throw new BackupException("MySQL connection failed", e);
        }
    }

    // ─── Full backup ───────────────────────────────────────────────────────────

    @Override
    public File performBackup(BackupRequest request, String tempFilePath) {
        try {
            File outputFile = new File(tempFilePath);
            outputFile.getParentFile().mkdirs();

            List<String> command = new ArrayList<>();
            command.add("mysqldump");
            command.add("-h"); command.add(request.getHost());
            command.add("-P"); command.add(String.valueOf(request.getPort()));
            command.add("-u"); command.add(request.getUsername());
            command.add("-p" + request.getPassword());
            command.add(request.getDbName());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false); //keep stderr separate from SQL output

            Process process = pb.start();

            // Drain stderr in background so warnings don't bleed into the SQL file
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("[mysqldump] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Write ONLY clean SQL stdout to file
            try (
                    InputStream inputStream = process.getInputStream();
                    FileOutputStream fos = new FileOutputStream(outputFile)
            ) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
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

    // ─── Incremental backup ────────────────────────────────────────────────────

    @Override
    public File performIncrementalBackup(BackupRequest request, String outputPath) throws Exception {
        if (request.getTables() == null || request.getTables().isEmpty()) {
            throw new BackupException("Incremental backup requires selected tables.");
        }

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
                throw new BackupException("The following tables do not exist in the database: " + nonExistentTables);
            }
        }

        List<String> command = new ArrayList<>();
        command.add("mysqldump");
        command.add("-h"); command.add(request.getHost());
        command.add("-P"); command.add(String.valueOf(request.getPort()));
        command.add("-u"); command.add(request.getUsername());
        command.add("-p" + request.getPassword());
        command.add(request.getDbName());
        command.addAll(request.getTables());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(new File(outputPath));
        pb.redirectErrorStream(false); // fix here too

        Process process = pb.start();

// Drain stderr
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("[mysqldump incremental] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new BackupException("Incremental backup failed. mysqldump exit code: " + exitCode);
        }

        return new File(outputPath);
    }

    // ─── Compress ──────────────────────────────────────────────────────────────

    @Override
    public File compressBackup(File inputFile) throws Exception {
        String gzipFilePath = inputFile.getAbsolutePath() + ".gz";

        try (
                FileInputStream fis = new FileInputStream(inputFile);
                FileOutputStream fos = new FileOutputStream(gzipFilePath);
                GZIPOutputStream gzipOS = new GZIPOutputStream(fos)
        ) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gzipOS.write(buffer, 0, len);
            }
        }

        // Delete the raw .sql file after compression
        inputFile.delete();

        return new File(gzipFilePath);
    }

    // ─── Decompress ────────────────────────────────────────────────────────────

    @Override
    public File decompressBackup(File compressedFile) throws Exception {
        String decompressedPath = compressedFile.getAbsolutePath();
        if (decompressedPath.endsWith(".gz")) {
            decompressedPath = decompressedPath.substring(0, decompressedPath.length() - 3);
        }

        File decompressedFile = new File(decompressedPath);

        try (
                GZIPInputStream gzipIn = new GZIPInputStream(
                        new BufferedInputStream(new FileInputStream(compressedFile)));
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(decompressedFile))
        ) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = gzipIn.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
        }

        return decompressedFile;
    }

    // ─── Restore ───────────────────────────────────────────────────────────────

    @Override
    public void restoreBackup(File dumpFile, RestoreRequest request) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("mysql");
        command.add("-h"); command.add(request.getTargetHost());
        command.add("-P"); command.add(String.valueOf(request.getTargetPort()));
        command.add("-u"); command.add(request.getTargetUsername());
        command.add("--password=" + request.getTargetPassword());
        command.add(request.getTargetDbName());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectInput(dumpFile);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String output = new String(process.getInputStream().readAllBytes());
        System.out.println(">>> mysql output: " + output);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new BackupException("mysql restore failed with exit code " + exitCode + " — " + output);
        }
    }
}