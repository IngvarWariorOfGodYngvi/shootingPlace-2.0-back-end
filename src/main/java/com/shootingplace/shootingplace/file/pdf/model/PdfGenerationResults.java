package com.shootingplace.shootingplace.file.pdf.model;

public record PdfGenerationResults(
        String fileName,
        byte[] data
) {
}
