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
import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.dateFormat;
import static com.shootingplace.shootingplace.file.pdf.PdfUtils.font;

@Component
@RequiredArgsConstructor
public class ErasedMembersByDatePdfGenerator {

    private final MemberRepository memberRepository;

    public PdfGenerationResults generate(LocalDate firstDate, LocalDate secondDate) throws DocumentException, IOException {

        String fileName = "Lista_osób_skreślonych_od_" + firstDate + "_do_" + secondDate + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        document.add(new Paragraph("Lista osób skreślonych od " + firstDate.format(dateFormat()) + " do " + secondDate.format(dateFormat()), font(14, 1)));
        document.add(new Paragraph("\n", font(14, 0)));

        List<MemberEntity> members = memberRepository.findAllByErasedTrue().stream().filter(m -> m.getErasedEntity() != null).filter(m -> m.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)) && m.getErasedEntity().getDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))).toList();

        float[] columnWidths = {4F, 28F, 10F, 14F, 14F, 36F};
        PdfPTable header = new PdfPTable(columnWidths);
        header.setWidthPercentage(100);

        header.addCell(headerCell("lp"));
        header.addCell(headerCell("Nazwisko Imię"));
        header.addCell(headerCell("legitymacja"));
        header.addCell(headerCell("PESEL"));
        header.addCell(headerCell("Przyczyna skreślenia"));
        header.addCell(headerCell("Informacje dodatkowe"));
        document.add(header);
        document.add(new Paragraph("\n", font(14, 0)));

        for (int i = 0; i < members.size(); i++) {
            MemberEntity m = members.get(i);
            PdfPTable row = new PdfPTable(columnWidths);
            row.setWidthPercentage(100);
            row.addCell(cell(String.valueOf(i + 1)));
            row.addCell(cell(m.getSecondName() + " " + m.getFirstName()));
            row.addCell(cell(String.valueOf(m.getLegitimationNumber())));
            row.addCell(cell(m.getPesel()));
            row.addCell(m.getErasedEntity() != null ? cell(m.getErasedEntity().getErasedType() + " " + m.getErasedEntity().getDate().format(dateFormat())) : cell(""));
            row.addCell(m.getErasedEntity() != null && m.getErasedEntity().getAdditionalDescription() != null ? cell(m.getErasedEntity().getAdditionalDescription()) : cell(""));

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
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

}
