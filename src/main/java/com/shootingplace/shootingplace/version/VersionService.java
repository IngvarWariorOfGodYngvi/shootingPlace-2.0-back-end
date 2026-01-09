package com.shootingplace.shootingplace.version;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class VersionService {

    @Value("${update.agent.dir}")
    private String agentDir;

    public String getCurrentVersion() {
        try {
            Path versionFile = Paths.get(agentDir, "version.txt");

            if (!Files.exists(versionFile)) {
                return "unknown";
            }

            return Files.readString(versionFile).trim();

        } catch (Exception e) {
            return "error";
        }
    }
}

