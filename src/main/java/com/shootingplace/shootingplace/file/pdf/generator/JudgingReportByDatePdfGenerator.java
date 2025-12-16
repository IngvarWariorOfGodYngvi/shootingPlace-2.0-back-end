package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.history.JudgingHistoryEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.dateFormat;
import static com.shootingplace.shootingplace.file.pdf.PdfUtils.font;

@Component
public class JudgingReportByDatePdfGenerator {

    private final MemberRepository memberRepository;

    public JudgingReportByDatePdfGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    public PdfGenerationResults generate(LocalDate firstDate, LocalDate secondDate) throws DocumentException, IOException {
        String fileName = "raport_sędziowania.pdf";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        document.add(new Paragraph("Raport sędziowania", font(13, 1)));
        document.add(new Paragraph("\n", font(10, 0)));

        List<MemberEntity> arbiters = memberRepository.findAll().stream().filter(m -> !m.isErased()).filter(m -> m.getMemberPermissions().getArbiterNumber() != null).toList();

        for (MemberEntity arbiter : arbiters) {
            if (arbiter.getHistory().getJudgingHistory().isEmpty()) {
                continue;
            }
            List<JudgingHistoryEntity> history = arbiter.getHistory().getJudgingHistory().stream().filter(h -> h.getDate().isAfter(firstDate) && h.getDate().isBefore(secondDate)).toList();
            if (history.isEmpty()) {
                continue;
            }
            document.add(new Paragraph(arbiter.getFirstName() + " " + arbiter.getSecondName(), font(10, 1)));
            for (int i = 0; i < history.size(); i++) {
                JudgingHistoryEntity h = history.get(i);
                Paragraph line = new Paragraph();
                line.add(new Chunk((i + 1) + " ", font(10, 0)));
                line.add(new Chunk(h.getName() + " ", font(10, 0)));
                line.add(new Chunk(h.getDate().format(dateFormat()) + " ", font(10, 0)));
                line.add(new Chunk(h.getJudgingFunction(), font(10, 0)));

                document.add(line);
            }
            document.add(new Paragraph("\n", font(8, 0)));
        }

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }
}
