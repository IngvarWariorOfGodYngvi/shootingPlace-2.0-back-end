package com.shootingplace.shootingplace.configurations;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class UpdateService {
    private final Logger LOG = LogManager.getLogger();
    public UpdateService() {
    }
    public void startUpdateAgent() {
        try {
            new ProcessBuilder("java", "-jar", "C:/update-agent/update-agent-1.0.0.jar").directory(new File("C:/update-agent")).start();
        } catch (IOException e) {
            LOG.error("Failed to start update agent", e);
        }
    }
}
