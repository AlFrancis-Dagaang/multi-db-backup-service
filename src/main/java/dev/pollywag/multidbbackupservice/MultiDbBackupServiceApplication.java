package dev.pollywag.multidbbackupservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;


@SpringBootApplication
public class MultiDbBackupServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiDbBackupServiceApplication.class, args);
    }
}
