package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.DocumentException;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;

import java.io.IOException;

public interface PdfGenerator<T> {
    PdfGenerationResults generate(T data) throws DocumentException, IOException;
}
