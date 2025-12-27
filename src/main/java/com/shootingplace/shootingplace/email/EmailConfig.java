package com.shootingplace.shootingplace.email;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
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

}

