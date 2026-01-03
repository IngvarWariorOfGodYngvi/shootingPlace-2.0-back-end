package com.shootingplace.shootingplace.configurations;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UpdateService {

    private final Logger log = LogManager.getLogger();
    private final UpdateAgentProperties props;

    public void     startUpdateAgent() {
        try {
            File dir = new File(props.getDir());

            new ProcessBuilder("java", "-jar", props.getJar()).directory(dir).start();

            log.info("Update Agent started from {}", dir.getAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to start Update Agent", e);
            throw new IllegalStateException("Cannot start update agent", e);
        }
    }
}

