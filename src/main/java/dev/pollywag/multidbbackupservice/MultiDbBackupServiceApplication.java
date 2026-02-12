package dev.pollywag.multidbbackupservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MultiDbBackupServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiDbBackupServiceApplication.class, args);
    }

}
