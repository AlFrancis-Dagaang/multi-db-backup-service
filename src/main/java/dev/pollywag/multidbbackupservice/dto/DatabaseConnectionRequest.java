package dev.pollywag.multidbbackupservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor // needed for JSON deserialization
public class DatabaseConnectionRequest {
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    
    public DatabaseConnectionRequest(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }


}
