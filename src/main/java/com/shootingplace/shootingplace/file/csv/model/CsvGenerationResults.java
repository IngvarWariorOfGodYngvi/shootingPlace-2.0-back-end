package com.shootingplace.shootingplace.file.csv.model;

public record CsvGenerationResults(
        String fileName,
        byte[] data
) {
}
