package com.shootingplace.shootingplace;

import com.shootingplace.shootingplace.settings.ApplicationLicenseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(ApplicationLicenseProperties.class)
public class ShootingplaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShootingplaceApplication.class, args);
    }

}
