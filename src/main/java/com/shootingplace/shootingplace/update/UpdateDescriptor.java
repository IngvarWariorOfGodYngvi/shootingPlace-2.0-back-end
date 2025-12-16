package com.shootingplace.shootingplace.update;

import lombok.Data;

@Data
public class UpdateDescriptor {

    private String version;
    private Artifact backend;
    private Artifact frontend;
    private int minJava;

    @Data
    public static class Artifact {
        private String type;   // war | zip
        private String url;
        private String checksum;
    }
}
