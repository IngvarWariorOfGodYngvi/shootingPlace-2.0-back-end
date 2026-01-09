package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Component
@RequiredArgsConstructor
public class MembersWithLicensePdfGenerator {

    private final MemberRepository memberRepository;

    public PdfGenerationResults generate() throws DocumentException, IOException {

        String fileName = "Lista osób z licencjami.pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<MemberEntity> all = memberRepository.findAllByErasedFalse().stream().filter(m -> m.getClub().getId().equals(1)).filter(m -> m.getLicense().getNumber() != null).filter(m -> m.getLicense().isValid()).toList();

        Paragraph newLine = new Paragraph("\n", font(13, 0));

        float[] columnWidths = {4F, 28F, 10F, 14F, 14F, 14F};

        PdfPTable header = createHeader(columnWidths);

        // ===== DOROŚLI =====
        Paragraph titleAdult = new Paragraph("Lista osób z licencjami - OGÓLNA", font(13, 0));
        titleAdult.setAlignment(Element.ALIGN_CENTER);

        document.add(titleAdult);
        document.add(newLine);
        document.add(header);
        document.add(newLine);

        List<MemberEntity> adults = all.stream().filter(MemberEntity::isAdult).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).toList();

        addMembersTable(document, adults, columnWidths);

        document.newPage();

        Paragraph titleYouth = new Paragraph("Lista osób z licencjami - Młodzież", font(13, 0));
        titleYouth.setAlignment(Element.ALIGN_CENTER);

        document.add(titleYouth);
        document.add(newLine);
        document.add(header);
        document.add(newLine);

        List<MemberEntity> youth = all.stream().filter(m -> !m.isAdult()).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).toList();

        addMembersTable(document, youth, columnWidths);

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private PdfPTable createHeader(float[] widths) throws IOException {
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);

        table.addCell(cell("lp"));
        table.addCell(cell("Nazwisko Imię"));
        table.addCell(cell("numer licencji"));
        table.addCell(cell("licencja ważna do"));
        table.addCell(cell("składki"));
        table.addCell(cell(""));

        return table;
    }

    private void addMembersTable(Document document, List<MemberEntity> members, float[] widths) throws DocumentException, IOException {

        for (int i = 0; i < members.size(); i++) {
            MemberEntity m = members.get(i);

            PdfPTable row = new PdfPTable(widths);
            row.setWidthPercentage(100);

            row.addCell(cell(String.valueOf(i + 1)));
            row.addCell(cell(m.getSecondName() + " " + m.getFirstName()));
            row.addCell(cell(m.getLicense().getNumber()));
            row.addCell(cell(String.valueOf(m.getLicense().getValidThru().getYear())));
            row.addCell(cell(m.isActive() ? "Aktywny" : "Brak składek"));
            row.addCell(cell(""));

            document.add(row);
        }
    }

    private PdfPCell cell(String text) throws IOException {
        return new PdfPCell(new Paragraph(text, font(12, 0)));
    }
}

