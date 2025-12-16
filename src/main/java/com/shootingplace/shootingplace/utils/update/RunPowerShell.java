package com.shootingplace.shootingplace.utils.update;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class RunPowerShell {

    private final Logger LOG = LogManager.getLogger();
    private final Environment env;

    public ResponseEntity<?> code;

    public RunPowerShell(Environment env, ResponseEntity<?> code) throws IOException {
        this.env = env;
        this.code = code;
        runUpdate();
    }

    public void runUpdate() throws IOException {

        if (this.code.getStatusCode().equals(HttpStatus.OK)) {
            LOG.info("RunPowerShell");

            LOG.info("Start: Front-end");
            new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-File", "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/downloadfront.ps1")
                    .inheritIO()
                    .start();
            LOG.info("Stop: Front-end");

            LOG.info("Start: Back-end");
            switch (env.getActiveProfiles()[0]) {
                case "prod":
                    new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-File", "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/downloadbackdziesiatka.ps1")
                            .inheritIO()
                            .start();
                    break;
                case "rcs":
                    new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-File", "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/downloadbackpanaszew.ps1")
                            .inheritIO()
                            .start();
                    break;
                default:
                    new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-File", "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/downloadbackdziesiatka.ps1")
                            .inheritIO()
                            .start();
                    break;
            }
            LOG.info("Stop: Back-end");
        }

    }
}
