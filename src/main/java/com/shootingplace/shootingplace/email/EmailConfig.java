package com.shootingplace.shootingplace.email;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Entity
public class EmailConfig {
    @Id
    @UuidGenerator
    private String uuid;
    private String connectionName;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private boolean auth;
    private boolean starttls;
    private String sslTrust;

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public void setStarttls(boolean starttls) {
        this.starttls = starttls;
    }

    public void setSslTrust(String sslTrust) {
        this.sslTrust = sslTrust;
    }
}

