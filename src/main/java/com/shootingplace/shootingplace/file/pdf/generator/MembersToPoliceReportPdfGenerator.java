package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.*;

@Component
public class MembersToPoliceReportPdfGenerator {

    private final MemberRepository memberRepository;

    public MembersToPoliceReportPdfGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public PdfGenerationResults generate() throws DocumentException, IOException {

        String fileName = "Lista_osób_do_zgłoszenia_na_Policję_" + LocalDate.now().format(dateFormat()) + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        String now = LocalTime.now().getHour() + ":" + String.format("%02d", LocalTime.now().getMinute());

        document.add(new Paragraph("Lista osób do zgłoszenia na policję " + LocalDate.now().format(dateFormat()) + " " + now, font(14, Font.BOLD)));

        document.add(new Paragraph("\n", font(14, Font.NORMAL)));

        LocalDate notValidSince = LocalDate.now().minusMonths(6);

        List<MemberEntity> members = memberRepository.findAll().stream().filter(m -> !m.isErased()).filter(m -> m.getClub().getId() == 1).filter(m -> m.getLicense().getNumber() != null).filter(m -> !m.getLicense().isValid()).filter(m -> m.getLicense().getValidThru().isBefore(notValidSince)).sorted(Comparator.comparing(MemberEntity::getSecondName, pl())).toList();

        float[] columnWidths = {7F, 44F, 17F, 17F};

        PdfPTable header = new PdfPTable(columnWidths);
        header.setWidthPercentage(100);

        header.addCell(cell("lp"));
        header.addCell(cell("Nazwisko Imię"));
        header.addCell(cell("PESEL"));
        header.addCell(cell("numer licencji"));

        document.add(header);
        document.add(new Paragraph("\n", font(12, Font.NORMAL)));

        for (int i = 0; i < members.size(); i++) {
            MemberEntity m = members.get(i);

            PdfPTable row = new PdfPTable(columnWidths);
            row.setWidthPercentage(100);

            row.addCell(cell(String.valueOf(i + 1)));
            row.addCell(cell(m.getSecondName() + " " + m.getFirstName()));
            row.addCell(cell(m.getPesel()));
            row.addCell(cell(m.getLicense().getNumber()));

            document.add(row);
        }

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private PdfPCell cell(String text) throws IOException {
        return new PdfPCell(new Paragraph(text, font(12, Font.NORMAL)));
    }
}
