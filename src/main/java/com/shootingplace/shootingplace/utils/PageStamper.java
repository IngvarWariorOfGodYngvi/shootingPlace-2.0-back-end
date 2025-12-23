package com.shootingplace.shootingplace.utils;

import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
@RequiredArgsConstructor
public class PageStamper extends PdfPageEventHelper {
    private final Environment environment;
    private final Boolean isPageNumberStamp;
    private final Boolean isFooterImage;
    int pages;

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        pages = writer.getPageNumber();
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            Rectangle pageSize = document.getPageSize();
            PdfContentByte directContent = writer.getDirectContent();
            document.addAuthor("Igor Żebrowski");
            if (isFooterImage) {
                URL resource = null;
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                    resource = getClass().getClassLoader().getResource("pełna-nazwa(małe).bmp");
                }
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
                    resource = getClass().getClassLoader().getResource("logo-panaszew.jpg");
                }
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.MECHANIK.getName())) {
                    resource = getClass().getClassLoader().getResource("logo-uks.jpg");
                }
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.GUARDIANS.getName())) {
                    resource = getClass().getClassLoader().getResource("logo-guardians.jpg");
                }
                Image image = Image.getInstance(resource);
                image.scaleToFit(1000, 75);
                float pw = pageSize.getWidth() / 2;
                float iw = image.getScaledWidth() / 2;
                float[] position = {pw - iw, 0};
                image.setAbsolutePosition(position[0], position[1]);
                directContent.addImage(image);
            }
            if (isPageNumberStamp) {
                final int currentPageNumber = writer.getCurrentPageNumber();
                pageSize = document.getPageSize();
                directContent = writer.getDirectContent();
                directContent.setColorFill(Color.BLACK);
                directContent.setFontAndSize(BaseFont.createFont(), 10);
                PdfTextArray pdfTextArray = new PdfTextArray(String.valueOf(currentPageNumber));
                directContent.setTextMatrix(pageSize.getRight(40), pageSize.getBottom(25));
                directContent.showText(pdfTextArray);
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
