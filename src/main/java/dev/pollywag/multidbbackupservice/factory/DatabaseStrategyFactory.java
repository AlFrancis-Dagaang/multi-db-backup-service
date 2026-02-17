package dev.pollywag.multidbbackupservice.factory;

import dev.pollywag.multidbbackupservice.exception.BackupException;
import dev.pollywag.multidbbackupservice.model.enums.DatabaseType;
import dev.pollywag.multidbbackupservice.strategy.database.DatabaseBackupStrategy;
import dev.pollywag.multidbbackupservice.strategy.database.MysqlBackupStrategy;
import org.springframework.stereotype.Component;

@Component
public class DatabaseStrategyFactory {
    private final MysqlBackupStrategy mysqlStrategy;

    public DatabaseStrategyFactory(MysqlBackupStrategy mysqlStrategy) {
        this.mysqlStrategy = mysqlStrategy;
    }

    public DatabaseBackupStrategy getStrategy(DatabaseType type) {
        return switch (type) {
            case MYSQL -> mysqlStrategy;
            default -> throw new BackupException("Unsupported DB type: " + type);
        };
    }
}
