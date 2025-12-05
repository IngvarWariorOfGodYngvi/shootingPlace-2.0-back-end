package com.shootingplace.shootingplace.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URL;

public class PageStamper extends PdfPageEventHelper {
    private final Environment environment;
    private final Boolean isPageNumberStamp;
    private final Boolean isPageStampEvent;
    int pages;

    public PageStamper(Environment environment, Boolean isPageNumberStamp, Boolean isPageStampEvent) {
        this.environment = environment;
        this.isPageNumberStamp = isPageNumberStamp;
        this.isPageStampEvent = isPageStampEvent;
    }

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
            if (isPageStampEvent) {
//                String source = "";
                URL resource = null;
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                    resource = getClass().getClassLoader().getResource("pełna-nazwa(małe).bmp");
//                    source = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/shootingplace-1.0/WEB-INF/classes/pełna-nazwa(małe).bmp";
                }
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
                    resource = getClass().getClassLoader().getResource("logo-panaszew.jpg");
//                    source = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/shootingplace-1.0/WEB-INF/classes/logo-panaszew.jpg";
                }
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.MECHANIK.getName())) {
                    resource = getClass().getClassLoader().getResource("logo-uks.jpg");
//                    source = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/shootingplace-1.0/WEB-INF/classes/logo-uks.jpg";
                }
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.GUARDIANS.getName())) {
                    resource = getClass().getClassLoader().getResource("logo-guardians.jpg");
//                    source = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/shootingplace-1.0/WEB-INF/classes/logo-uks.jpg";
                }
                Image image = Image.getInstance(resource);
                int multiplicity = 7;
                image.scaleAbsolute(new Rectangle(16 * multiplicity, 9 * multiplicity));
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
                directContent.setColorFill(BaseColor.BLACK);
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
