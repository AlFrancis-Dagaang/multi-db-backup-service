package dev.pollywag.multidbbackupservice.repository;

import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackupLogRepository
        extends JpaRepository<BackupLog, String> {
}
