package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedToEvidenceEntity;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
@RequiredArgsConstructor
public class AmmunitionListPdfGenerator {

    private final ClubRepository clubRepository;
    private final Environment environment;

    public PdfGenerationResults generate(AmmoEvidenceEntity ammoEvidenceEntity) throws DocumentException, IOException {

        ClubEntity club = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);

        List<AmmoInEvidenceEntity> a = ammoEvidenceEntity.getAmmoInEvidenceEntityList();

        String[] sort = {"5,6mm", "9x19mm", "12/76", ".357", ".38", "7,62x39mm"};
        List<AmmoInEvidenceEntity> ordered = new ArrayList<>();

        for (String s : sort) {
            a.stream().filter(f -> f.getCaliberName().equals(s)).findFirst().ifPresent(ordered::add);
        }

        List<AmmoInEvidenceEntity> rest = a.stream().filter(f -> Arrays.stream(sort).noneMatch(s -> s.equals(f.getCaliberName()))).toList();

        ordered.addAll(rest);

        String fileName = "Lista_Amunicyjna_" + ammoEvidenceEntity.getDate().format(dateFormat()) + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, true, PageStampMode.A4));

        document.open();

        Paragraph number = new Paragraph(ammoEvidenceEntity.getNumber(), font(10, 4));
        Paragraph p = new Paragraph(club.getFullName() + "\n", font(14, 1));
        Paragraph p1 = new Paragraph("Lista rozliczenia amunicji " + ammoEvidenceEntity.getDate().format(dateFormat()), font(12, 2));

        number.setIndentationLeft(450);
        p.setAlignment(Element.ALIGN_CENTER);
        p1.setAlignment(Element.ALIGN_CENTER);

        document.add(number);
        document.add(p);
        document.add(p1);

        float[] widths = {20F, 255F, 25};

        for (AmmoInEvidenceEntity ammo : ordered) {

            Paragraph p2 = new Paragraph("Kaliber: " + ammo.getCaliberName(), font(12, 1));
            p2.setIndentationLeft(230);
            p2.setSpacingBefore(10);
            document.add(p2);
            document.add(new Paragraph(" ",font(6,0)));
            PdfPTable tableLabel = new PdfPTable(widths);
            tableLabel.addCell(new PdfPCell(new Paragraph("lp.", font(10, 2))));
            tableLabel.addCell(new PdfPCell(new Paragraph("Imię i Nazwisko", font(10, 2))));
            tableLabel.addCell(new PdfPCell(new Paragraph("ilość sztuk", font(10, 2))));
            document.add(tableLabel);

            for (int j = 0; j < ammo.getAmmoUsedToEvidenceEntityList().size(); j++) {

                AmmoUsedToEvidenceEntity used = ammo.getAmmoUsedToEvidenceEntityList().get(j);

                String name;
                if (used.getMemberEntity() == null) {
                    OtherPersonEntity op = used.getOtherPersonEntity();
                    name = op.getSecondName() + " " + op.getFirstName();
                } else {
                    MemberEntity me = used.getMemberEntity();
                    name = me.getSecondName() + " " + me.getFirstName();
                }

                PdfPTable table = new PdfPTable(widths);
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(j + 1), font(10, 2))));
                table.addCell(new PdfPCell(new Paragraph(name, font(10, 2))));
                table.addCell(new PdfPCell(new Paragraph(used.getCounter().toString(), font(10, 2))));
                document.add(table);
            }

            PdfPTable tableSum = new PdfPTable(widths);

            PdfPCell sumEmpty = new PdfPCell(new Paragraph(""));
            PdfPCell sumLabel = new PdfPCell(new Paragraph("Suma", font(10, 2)));
            PdfPCell sumValue = new PdfPCell(new Paragraph(ammo.getQuantity().toString(), font(10, 2)));

            sumEmpty.setBorder(0);
            sumLabel.setBorder(0);
            sumLabel.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            tableSum.addCell(sumEmpty);
            tableSum.addCell(sumLabel);
            tableSum.addCell(sumValue);
            document.add(tableSum);
        }

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }
}
