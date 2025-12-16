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
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.*;

@Component
public class AllMembersAttendancePdfGenerator {

    private static final float[] COLUMN_WIDTHS = {
            3F, 25F, 15F, 20F
    };

    private final MemberRepository memberRepository;

    public AllMembersAttendancePdfGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public PdfGenerationResults generate()
            throws DocumentException, IOException {

        String fileName =
                "Lista_obecności_klubowiczów_" +
                        LocalDate.now().format(dateFormat()) +
                        ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.setMarginMirroringTopBottom(true);
        document.addTitle(fileName);
        document.addCreationDate();

        List<MemberEntity> members = memberRepository.findAll().stream()
                .filter(m -> !m.isErased())
                .sorted(
                        Comparator
                                .comparing(MemberEntity::getSecondName, pl())
                                .thenComparing(MemberEntity::getFirstName, pl())
                )
                .toList();

        document.add(new Paragraph(
                "Lista obecności klubowiczów na dzień " +
                        LocalDate.now().format(dateFormat()),
                font(14, Font.BOLD)
        ));
        document.add(new Paragraph("\n", font(14, Font.NORMAL)));

        PdfPTable table = new PdfPTable(COLUMN_WIDTHS);
        table.setWidthPercentage(100);

        addHeader(table);

        for (int i = 0; i < members.size(); i++) {
            addRow(table, members.get(i), i + 1);
        }

        document.add(table);
        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }


    private void addHeader(PdfPTable table) throws IOException {
        table.addCell(centerCell("lp"));
        table.addCell(centerCell("Nazwisko Imię"));
        table.addCell(centerCell("Legitymacja"));
        table.addCell(centerCell("Podpis"));
    }

    private void addRow(PdfPTable table, MemberEntity m, int index) throws IOException {
        table.addCell(centerCell(String.valueOf(index)));
        table.addCell(cell(
                m.getSecondName() + " " + m.getFirstName()
        ));
        table.addCell(centerCell(
                String.valueOf(m.getLegitimationNumber())
        ));
        table.addCell(cell(" "));
    }

    private PdfPCell cell(String text) throws IOException {
        return new PdfPCell(
                new Paragraph(text, font(12, Font.NORMAL))
        );
    }

    private PdfPCell centerCell(String text) throws IOException {
        PdfPCell cell = cell(text);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}
