package com.shootingplace.shootingplace.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "update.agent")
@Getter
@Setter
public class UpdateAgentProperties {
    private String dir;
    private String jar;
}
