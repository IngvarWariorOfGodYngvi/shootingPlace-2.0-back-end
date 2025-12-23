package com.shootingplace.shootingplace.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "update.agent")
@Data
public class UpdateAgentProperties {
    private String dir;
    private String jar;
}
