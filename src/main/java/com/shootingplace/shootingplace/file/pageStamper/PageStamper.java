package com.shootingplace.shootingplace.file.pageStamper;

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
    private final PageStampMode mode;
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
            Rectangle pageSize;
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
                Rectangle ps = document.getPageSize();

                switch (mode) {
                    case CARD -> {
                        image.scaleToFit(ps.getWidth() * 0.6f, ps.getHeight() * 0.18f);
                        image.setAbsolutePosition(((ps.getWidth() / 2) - (image.getScaledWidth()) / 2), 0);
                        directContent.addImage(image);

                    }

                    case A5_LANDSCAPE -> {
                        image.scaleToFit(700, 60);
                        image.setAbsolutePosition(((ps.getWidth() / 2) - (image.getScaledWidth()) / 2), 0);
                        directContent.addImage(image);
                    }

                    case A4_LANDSCAPE -> {
                        image.scaleToFit(900, 60);
                        image.setAbsolutePosition(((ps.getWidth() / 2) - (image.getScaledWidth()) / 2), 0);
                        directContent.addImage(image);
                    }

                    case A4 -> {
                        image.scaleToFit(1000, 65);
                        image.setAbsolutePosition(((ps.getWidth() / 2) - (image.getScaledWidth()) / 2), 0);
                        directContent.addImage(image);
                    }
                }
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