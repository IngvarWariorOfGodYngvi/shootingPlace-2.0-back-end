package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
public class MembersToErasedListPdfGenerator {

    private final MemberRepository memberRepository;

    public MembersToErasedListPdfGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public PdfGenerationResults generate() throws DocumentException, IOException {

        String fileName = "Lista osób do skreślenia na dzień " + LocalDate.now().format(dateFormat()) + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        document.add(new Paragraph("Lista osób do skreślenia na dzień " + LocalDate.now().format(dateFormat()), font(14, 1)));
        document.add(new Paragraph("\n", font(14, 0)));

        LocalDate notValidDate = LocalDate.now().minusMonths(6);

        List<MemberEntity> members = memberRepository.findAllByErasedFalseAndActiveFalse().stream().filter(m -> m.getHistory().getContributionList().isEmpty() || m.getHistory().getContributionList().getFirst().getValidThru().minusDays(1).isBefore(notValidDate)).sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))).toList();

        float[] columnWidths = {4F, 42F, 14F, 14F, 14F, 14F};

        PdfPTable header = new PdfPTable(columnWidths);
        header.setWidthPercentage(100);

        header.addCell(cell("lp"));
        header.addCell(cell("Nazwisko Imię"));
        header.addCell(cell("legitymacja"));
        header.addCell(cell("numer licencji"));
        header.addCell(cell("licencja ważna do"));
        header.addCell(cell("Składka ważna do"));

        document.add(header);
        document.add(new Paragraph("\n", font(12, 0)));

        for (int i = 0; i < members.size(); i++) {
            MemberEntity m = members.get(i);

            PdfPTable row = new PdfPTable(columnWidths);
            row.setWidthPercentage(100);

            row.addCell(cell(String.valueOf(i + 1)));
            row.addCell(cell(m.getSecondName() + " " + m.getFirstName()));
            row.addCell(cell(String.valueOf(m.getLegitimationNumber())));

            row.addCell(cell(m.getLicense().getNumber() != null ? m.getLicense().getNumber() : ""));

            row.addCell(cell(m.getLicense().getNumber() != null ? String.valueOf(m.getLicense().getValidThru()) : ""));

            row.addCell(cell(!m.getHistory().getContributionList().isEmpty() ? String.valueOf(m.getHistory().getContributionList().getFirst().getValidThru()) : "BRAK SKŁADEK"));

            document.add(row);
        }

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private PdfPCell cell(String text) throws IOException {
        return new PdfPCell(new Paragraph(text, font(12, 0)));
    }
}
