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

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.*;

@Component
public class MembersListByAdultPdfGenerator implements PdfGenerator<Boolean> {

    private static final float[] COLUMN_WIDTHS = {4F, 58F, 10F, 12F, 12F, 12F};

    private final MemberRepository memberRepository;

    public MembersListByAdultPdfGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public PdfGenerationResults generate(Boolean adult) throws DocumentException, IOException {

        String fileName = "Lista_klubowiczów_na_dzień_" + LocalDate.now().format(dateFormat()) + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<MemberEntity> members = memberRepository.findAll().stream().filter(m -> !m.isErased()).filter(m -> m.isAdult() == adult).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).toList();

        document.add(new Paragraph("Lista klubowiczów na dzień " + LocalDate.now().format(dateFormat()), font(14, Font.BOLD)));
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
        table.addCell(cell("lp", Font.NORMAL));
        table.addCell(cell("Nazwisko Imię", Font.NORMAL));
        table.addCell(cell("legitymacja", Font.NORMAL));
        table.addCell(cell("Data zapisu", Font.NORMAL));
        table.addCell(cell("Data opłacenia składki", Font.NORMAL));
        table.addCell(cell("Składka ważna do", Font.NORMAL));
    }

    private void addRow(PdfPTable table, MemberEntity m, int index) throws IOException {
        table.addCell(cell(String.valueOf(index), Font.NORMAL));
        table.addCell(cell(m.getSecondName() + " " + m.getFirstName(), Font.NORMAL));
        table.addCell(cell(String.valueOf(m.getLegitimationNumber()), Font.NORMAL));
        table.addCell(cell(String.valueOf(m.getJoinDate()), Font.NORMAL));

        PdfPCell payment;
        PdfPCell validThru;

        if (!m.getHistory().getContributionList().isEmpty()) {
            payment = cell(String.valueOf(m.getHistory().getContributionList().getFirst().getPaymentDay()), Font.NORMAL);
            validThru = cell(String.valueOf(m.getHistory().getContributionList().getFirst().getValidThru()), Font.NORMAL);
        } else {
            payment = cell("BRAK SKŁADEK", Font.BOLD);
            validThru = cell("BRAK SKŁADEK", Font.BOLD);
        }

        if (!m.isActive()) {
            payment.setBackgroundColor(Color.RED);
            validThru.setBackgroundColor(Color.RED);
        }

        table.addCell(payment);
        table.addCell(validThru);
    }

    private PdfPCell cell(String text, int style) throws IOException {
        return new PdfPCell(new Paragraph(text, font(12, style)));
    }
}
