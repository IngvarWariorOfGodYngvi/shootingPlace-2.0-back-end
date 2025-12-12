package com.shootingplace.shootingplace.file.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public final class PdfUtils {

    private PdfUtils() {
    }

    public static Font font(int size, int style) throws IOException, DocumentException {
        BaseFont base = BaseFont.createFont("font/times.ttf", BaseFont.IDENTITY_H, BaseFont.CACHED);
        return new Font(base, size, style);
    }

    public static DateTimeFormatter dateFormat() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy");
    }
}
