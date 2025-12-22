package com.shootingplace.shootingplace.settings;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;
@Getter
@Setter
@ConfigurationProperties(prefix = "application-license")
public class ApplicationLicenseProperties {

    private LocalDate endDate;
    private String signature;

}
