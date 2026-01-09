package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.FilesRepository;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Component
@RequiredArgsConstructor
public class MemberLegitimationPdfGenerator {

    private final Environment environment;
    private final FilesRepository filesRepository;

    public PdfGenerationResults generate(MemberEntity member) throws DocumentException, IOException {

        String fileName = "Legitymacja_" + member.getSecondName() + ".pdf";

        // ID-1: 85.60 × 53.98 mm
        Rectangle pageSize = new Rectangle(Utilities.millimetersToPoints(85.60f), Utilities.millimetersToPoints(53.98f));
        pageSize.setRotation(90);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(pageSize, 6, 6, 6, 6);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        writer.setPageEvent(new PageStamper(environment, false, true, PageStampMode.CARD));

        document.open();
        document.addTitle(fileName);

        PdfContentByte cb = writer.getDirectContent();
        PdfContentByte cbUnder = writer.getDirectContentUnder();

        drawBackgroundGradient(cbUnder, document);
        drawFrame(cb, document);

        Font nameFont = font(14, Font.BOLD);
        Font clubFont = font(7, Font.BOLD);

        float centerX = document.getPageSize().getWidth() / 2;
        float headerTop = document.top() - 14;

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(member.getFullName(), nameFont), centerX, headerTop, 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(member.getClub().getFullName(), clubFont), centerX, headerTop - 14, 0);

        Image photo = null;
        if (member.getImageUUID() != null) {
            FilesEntity file = filesRepository.findById(member.getImageUUID()).orElseThrow(() -> new EntityNotFoundException("Image not found"));
            photo = Image.getInstance(file.getData());
        }

        float photoX = document.left() + 10;
        float photoY = document.bottom() + 14;
        float photoW = 72;
        float photoH = 96;

        if (photo != null) {
            photo.scaleToFit(photoW, photoH);
            photo.setAbsolutePosition(photoX, photoY);
            document.add(photo);

            cb.setLineWidth(0.5f);
            cb.setColorStroke(Color.LIGHT_GRAY);
            float realW = photo.getScaledWidth();
            float realH = photo.getScaledHeight();
            cb.rectangle(photoX, photoY, realW, realH);
            cb.stroke();
        }

        Font normal = font(9, Font.NORMAL);
        Font small = font(8, Font.NORMAL);

        float textX = photoX + photoW + 14;
        float textY = document.top() - 40;
        float line = 13;

        text(cb, "Nr legitymacji: " + member.getLegitimationNumber(), normal, textX, textY);
        text(cb, "Grupa: " + member.getMemberEntityGroup().getName(), normal, textX, textY - line);
        text(cb, "Data urodzenia: " + member.getBirthDate(), small, textX, textY - 2 * line);

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private static void text(PdfContentByte cb, String value, Font font, float x, float y) {
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(value, font), x, y, 0);
    }

    private void drawFrame(PdfContentByte cb, Document document) {
        cb.saveState();
        cb.setLineWidth(1.0f);
        ProfilesEnum activeProfile = ProfilesEnum.fromName(environment.getActiveProfiles()[0]);
        switch (activeProfile) {
            case ProfilesEnum.TEST -> cb.setColorStroke(new Color(253, 77, 33));
            case ProfilesEnum.DZIESIATKA -> cb.setColorStroke(new Color(135, 20, 33));
            case ProfilesEnum.PANASZEW -> cb.setColorStroke(new Color(0, 128, 0));
            case ProfilesEnum.MECHANIK -> cb.setColorStroke(new Color(19, 64, 132));
            case ProfilesEnum.GUARDIANS -> cb.setColorStroke(new Color(6, 45, 92));
        }

        cb.roundRectangle(document.left(), document.bottom(), document.right() - document.left(), document.top() - document.bottom(), 10f);
        cb.stroke();
        cb.restoreState();
    }

    private void drawBackgroundGradient(PdfContentByte cb, Document document) {
        Rectangle ps = document.getPageSize();

        float x0 = ps.getLeft();
        float y0 = ps.getBottom();
        float x1 = ps.getRight();
        float y1 = ps.getTop();

        Color start = null;
        ProfilesEnum activeProfile = ProfilesEnum.fromName(environment.getActiveProfiles()[0]);
        switch (activeProfile) {
            case ProfilesEnum.TEST -> start = new Color(9, 17, 35);
            case ProfilesEnum.DZIESIATKA -> start = new Color(55, 69, 80);
            case ProfilesEnum.PANASZEW -> start = new Color(160, 0, 0);
            case ProfilesEnum.MECHANIK -> start = new Color(78, 96, 127);
            case ProfilesEnum.GUARDIANS -> start = new Color(17, 148, 217);
        }
        Color end = Color.WHITE;

        PdfShading shading = PdfShading.simpleAxial(cb.getPdfWriter(), x0, y0, x1, y1, start, end);

        PdfShadingPattern pattern = new PdfShadingPattern(shading);

        cb.saveState();
        cb.setShadingFill(pattern);
        cb.rectangle(x0, y0, ps.getWidth(), ps.getHeight());
        cb.fill();
        cb.restoreState();
    }
}