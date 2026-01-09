package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Component
@RequiredArgsConstructor
public class GunTransportCertificatePdfGenerator {

    private final GunRepository gunRepository;

    public PdfGenerationResults generate(List<String> guns, LocalDate firstDate, LocalDate secondDate) throws DocumentException, IOException {

        String fileName = "Lista_broni_do_przewozu_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        String minute = LocalTime.now().getMinute() < 10 ? "0" + LocalTime.now().getMinute() : String.valueOf(LocalTime.now().getMinute());

        String now = LocalTime.now().getHour() + ":" + minute;
        document.add(new Paragraph("Lista jednostek broni do przewozu od dnia " + firstDate + " do dnia " + secondDate, font(14, 1)));
        document.add(new Paragraph("Wystawiono dnia " + LocalDate.now().format(dateFormat()) + " o godzinie " + now, font(14, 1)));
        document.add(new Paragraph("\n", font(14, 0)));

        float[] columnWidths = {4F, 16F, 12F, 12F, 12F, 12F, 12F};

        PdfPTable header = new PdfPTable(columnWidths);
        header.setWidthPercentage(100);

        header.addCell(headerCell("Lp"));
        header.addCell(headerCell("Marka i Model"));
        header.addCell(headerCell("Kaliber i rok produkcji"));
        header.addCell(headerCell("Numer i seria"));
        header.addCell(headerCell("Poz. z książki ewidencji"));
        header.addCell(headerCell("Magazynki"));
        header.addCell(headerCell("Numer świadectwa"));

        document.add(header);
        List<GunEntity> finalCollect = new ArrayList<>();
        guns.forEach(e -> finalCollect.add(gunRepository.getReferenceById(e)));

        for (int i = 0; i < guns.size(); i++) {
            GunEntity gun = finalCollect.get(i);

            PdfPTable row = new PdfPTable(columnWidths);
            row.setWidthPercentage(100);
            row.addCell(cell(String.valueOf(i + 1)));
            row.addCell(cell(gun.getModelName()));
            row.addCell(cell(gun.getProductionYear() != null && !gun.getProductionYear().isEmpty() && !"null".equals(gun.getProductionYear()) ? gun.getCaliber() + "\nrok " + gun.getProductionYear() : gun.getCaliber()));
            row.addCell(cell(gun.getSerialNumber()));
            row.addCell(cell(gun.getRecordInEvidenceBook()));
            row.addCell(cell(gun.getNumberOfMagazines()));
            row.addCell(cell(gun.getGunCertificateSerialNumber()));
            document.add(row);
        }
        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private PdfPCell headerCell(String text) throws IOException {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font(12, 0)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell cell(String text) throws IOException {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font(12, 0)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
}
