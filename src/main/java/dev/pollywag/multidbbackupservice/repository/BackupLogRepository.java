package dev.pollywag.multidbbackupservice.repository;

import dev.pollywag.multidbbackupservice.model.entity.BackupLog;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BackupLogRepository
        extends JpaRepository<BackupLog, String> {
    
    List<BackupLog> findByAction(String action);
    
    List<BackupLog> findByStatus(String status);
    
    List<BackupLog> findByDbName(String dbName);
    
    List<BackupLog> findByDbType(DatabaseType dbType);
    
    List<BackupLog> findByActionAndStatus(String action, String status);
    
    List<BackupLog> findByDbNameAndStatus(String dbName, String status);
    
    List<BackupLog> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT l FROM BackupLog l WHERE " +
           "(:action IS NULL OR l.action = :action) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:dbName IS NULL OR l.dbName = :dbName) AND " +
           "(:dbType IS NULL OR l.dbType = :dbType) AND " +
           "(:startDate IS NULL OR l.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR l.startTime <= :endDate)")
    List<BackupLog> findWithFilters(
            @Param("action") String action,
            @Param("status") String status,
            @Param("dbName") String dbName,
            @Param("dbType") DatabaseType dbType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}