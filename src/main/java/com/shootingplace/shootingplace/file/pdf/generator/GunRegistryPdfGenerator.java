package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.armory.GunStoreEntity;
import com.shootingplace.shootingplace.armory.GunStoreRepository;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Component
@RequiredArgsConstructor
public class GunRegistryPdfGenerator implements PdfGenerator<List<String>> {

    private final GunStoreRepository gunStoreRepository;
    private final GunRepository gunRepository;

    @Override
    public PdfGenerationResults generate(List<String> guns) throws DocumentException, IOException {

        String fileName = "Lista_broni_w_magazynie_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        document.add(new Paragraph("LISTA BRONI W MAGAZYNIE", font(14, 1)));
        document.add(new Paragraph("\n", font(14, 0)));

        float[] columnWidths = {4F, 16F, 10F, 10F, 10F, 10F, 10F};

        PdfPTable header = new PdfPTable(columnWidths);
        header.setWidthPercentage(100);
        header.addCell(headerCell("Lp"));
        header.addCell(headerCell("Marka i Model"));
        header.addCell(headerCell("Kaliber i rok produkcji"));
        header.addCell(headerCell("Numer i seria"));
        header.addCell(headerCell("Poz. z książki ewidencji"));
        header.addCell(headerCell("Magazynki"));
        header.addCell(headerCell("Numer świadectwa"));
        List<String> gunTypes = new ArrayList<>();

        for (String uuid : guns) {
            GunStoreEntity store = gunStoreRepository.findAll().stream().filter(g -> g.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
            if (!store.getGunEntityList().isEmpty()) {
                gunTypes.add(store.getTypeName());
            }
        }
        gunTypes.sort(String::compareTo);
        for (String type : gunTypes) {
            Paragraph typeTitle = new Paragraph(type, font(12, 1));
            typeTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(typeTitle);
            document.add(new Paragraph("\n", font(12, 0)));
            document.add(header);

            List<GunEntity> gunsInStock = gunRepository.findAll().stream().filter(g -> g.getGunType().equals(type)).filter(GunEntity::isInStock).sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName)).toList();

            for (int i = 0; i < gunsInStock.size(); i++) {
                GunEntity gun = gunsInStock.get(i);

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

            document.add(new Paragraph("\n", font(12, 0)));
        }

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private PdfPCell headerCell(String text) throws IOException {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font(10, 0)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell cell(String text) throws IOException {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font(8, 0)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
}
