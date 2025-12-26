package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordEntity;
import com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordRepository;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
@RequiredArgsConstructor
public class EvidenceBookByDatePdfGenerator {

    private final RegistrationRecordRepository registrationRecordRepository;

    public PdfGenerationResults generate(LocalDate firstDate, LocalDate secondDate, Map<String, byte[]> imagesByUuid) throws DocumentException, IOException {

        String fileName = "Książka_rejestru_pobytu_na_strzelnicy_od_" + firstDate + "_do_" + secondDate + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<RegistrationRecordEntity> records = registrationRecordRepository.findAll().stream().filter(r -> r.getDateTime().toLocalDate().isAfter(firstDate.minusDays(1)) && r.getDateTime().toLocalDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(RegistrationRecordEntity::getDateTime).reversed()).toList();

        document.add(new Paragraph("Książka rejestru pobytu na strzelnicy od " + firstDate + " do " + secondDate, font(13, 1)));
        document.add(new Phrase("\n"));

        float[] columns = {10, 30, 20, 30, 30};
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);

        table.addCell(headerCell("lp"));
        table.addCell(headerCell("Nazwisko i Imię"));
        table.addCell(headerCell("Data i godzina wejścia"));
        table.addCell(headerCell("Adres lub pozwolenie na broń"));
        table.addCell(headerCell("podpis"));

        for (int i = 0; i < records.size(); i++) {
            RegistrationRecordEntity r = records.get(i);
            table.addCell(indexCell(i + 1));
            table.addCell(textCell(r.getNameOnRecord()));
            table.addCell(textCell(r.getDateTime().toString().replace("T", " ").substring(0, 16)));
            table.addCell(textCell(r.getWeaponPermission() != null ? r.getWeaponPermission() : r.getAddress()));
            if (r.getImageUUID() != null && imagesByUuid.containsKey(r.getImageUUID())) {
                Image img = Image.getInstance(imagesByUuid.get(r.getImageUUID()));
                table.addCell(img);
            } else {
                table.addCell("");
            }
        }
        document.add(table);
        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private PdfPCell headerCell(String text) throws IOException {
        PdfPCell c = new PdfPCell(new Phrase(text, font(10, 0)));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    private PdfPCell textCell(String text) throws IOException {
        return new PdfPCell(new Phrase(text + " ", font(8, 0)));
    }

    private PdfPCell indexCell(int index) throws IOException {
        PdfPCell c = new PdfPCell(new Phrase(index + " ", font(8, 0)));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

}
