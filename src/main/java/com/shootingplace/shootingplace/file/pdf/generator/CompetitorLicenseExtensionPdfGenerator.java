package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.history.CompetitionHistoryEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.utils.PageStamper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.font;

@Component
public class CompetitorLicenseExtensionPdfGenerator
        implements PdfGenerator<MemberEntity> {

    private final ClubRepository clubRepository;
    private final Environment environment;

    public CompetitorLicenseExtensionPdfGenerator(
            ClubRepository clubRepository,
            Environment environment
    ) {
        this.clubRepository = clubRepository;
        this.environment = environment;
    }

    @Override
    public PdfGenerationResults generate(MemberEntity memberEntity)
            throws DocumentException, IOException {

        String fileName = "Wniosek " + memberEntity.getFullName() + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(35F, 35F, 50F, 50F);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, true));

        document.open();

        PdfContentByte cb = writer.getDirectContent();

        PdfReader reader =
                new PdfReader("Wniosek_o_przedluzenie_licencji_zawodniczej.pdf");
        PdfImportedPage page = writer.getImportedPage(reader, 1);
        cb.addTemplate(page, 0, 0);

        String licenseNumber = memberEntity.getLicense().getNumber();
        char[] P = memberEntity.getPesel().toCharArray();

        String name = memberEntity.getSecondName().toUpperCase() + "  "
                + memberEntity.getFirstName().toUpperCase();

        String phone = memberEntity.getPhoneNumber();
        String phoneSplit = phone.substring(0, 3) + " "
                + phone.substring(3, 6) + " "
                + phone.substring(6, 9) + " "
                + phone.substring(9, 12);

        String year =
                memberEntity.getLicense().getValidThru().toString().substring(2, 4);

        int licenceYear = memberEntity.getLicense().getValidThru().getYear();

        List<CompetitionHistoryEntity> history =
                memberEntity.getHistory().getCompetitionHistory();

        List<CompetitionHistoryEntity> collectPistol = new ArrayList<>();
        List<CompetitionHistoryEntity> collectRifle = new ArrayList<>();
        List<CompetitionHistoryEntity> collectShotgun = new ArrayList<>();

        for (CompetitionHistoryEntity entity : history) {
            if (entity.getDate().getYear() != licenceYear) continue;

            if (entity.isWZSS() && entity.getDiscipline() != null) {
                if (entity.getDiscipline().equals(Discipline.PISTOL.getName()))
                    collectPistol.add(entity);
                if (entity.getDiscipline().equals(Discipline.RIFLE.getName()))
                    collectRifle.add(entity);
                if (entity.getDiscipline().equals(Discipline.SHOTGUN.getName()))
                    collectShotgun.add(entity);
            }

            if (entity.getDisciplineList() != null) {
                for (String s : entity.getDisciplineList()) {
                    if (s.equals(Discipline.PISTOL.getName()))
                        collectPistol.add(entity);
                    if (s.equals(Discipline.RIFLE.getName()))
                        collectRifle.add(entity);
                    if (s.equals(Discipline.SHOTGUN.getName()))
                        collectShotgun.add(entity);
                }
            }
        }

        int pistol = memberEntity.getShootingPatent().getPistolPermission()
                ? collectPistol.size() : 0;

        int rifle = memberEntity.getShootingPatent().getRiflePermission()
                ? collectRifle.size() : 0;

        int shotgun = memberEntity.getShootingPatent().getShotgunPermission()
                ? collectShotgun.size() : 0;

        if (pistol >= 4) {
            rifle = Math.min(rifle, 2);
            shotgun = Math.min(shotgun, 2);
            pistol = 4;
        } else if (rifle >= 4) {
            pistol = Math.min(pistol, 2);
            shotgun = Math.min(shotgun, 2);
            rifle = 4;
        } else if (shotgun >= 4) {
            pistol = Math.min(pistol, 2);
            rifle = Math.min(rifle, 2);
            shotgun = 4;
        }

        Paragraph patentNumber = new Paragraph(
                memberEntity.getShootingPatent().getPatentNumber()
                        + "                                                       "
                        + licenseNumber,
                font(12, 0)
        );
        patentNumber.setIndentationLeft(160);

        Paragraph pesel = new Paragraph(
                P[0] + "   " + P[1] + "   " + P[2] + "   " + P[3] + "   " +
                        P[4] + "   " + P[5] + "  " + P[6] + "   " + P[7] + "   " +
                        P[8] + "   " + P[9] + "   " + P[10] +
                        "                                             " + phoneSplit,
                font(12, 0)
        );
        pesel.setIndentationLeft(72);

        Paragraph names = new Paragraph(name, font(12, 0));
        names.setIndentationLeft(150);

        Paragraph pYear = new Paragraph(year, font(12, 1));
        pYear.setIndentationLeft(350);

        for (int i = 0; i < 11; i++) document.add(new Paragraph("\n"));

        document.add(patentNumber);
        document.add(pesel);
        document.add(names);

        for (int i = 0; i < 5; i++) document.add(new Paragraph("\n"));

        document.add(pYear);
        document.add(new Paragraph("\n"));

        ClubEntity club =
                clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);

        int counter = 0;
        String activeProfile = environment.getActiveProfiles()[0];
        float fixedHeight = 27F;

        counter = addDisciplineRows(
                document, collectPistol, pistol, club,
                activeProfile, counter, fixedHeight,
                new float[]{50, 20, 20, 5, 10, 2, 28},
                true, false, false
        );

        counter = addDisciplineRows(
                document, collectRifle, rifle, club,
                activeProfile, counter, fixedHeight,
                new float[]{50, 20, 20, 5, 2, 10, 28},
                false, true, false
        );

        addDisciplineRows(
                document, collectShotgun, shotgun, club,
                activeProfile, counter, fixedHeight,
                new float[]{50, 20, 20, 8, 3, 6, 28},
                false, false, true
        );

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private int addDisciplineRows(
            Document document,
            List<CompetitionHistoryEntity> list,
            int limit,
            ClubEntity club,
            String activeProfile,
            int counter,
            float fixedHeight,
            float[] widths,
            boolean pistol,
            boolean rifle,
            boolean shotgun
    ) throws DocumentException, IOException {

        for (int i = 0; i < limit; i++) {

            PdfPTable table = new PdfPTable(widths);

            CompetitionHistoryEntity e = list.get(i);
            counter++;

            PdfPCell c1 = new PdfPCell(new Paragraph(
                    e.getName() + "\n" + club.getShortName(),
                    font(10, 0)
            ));
            PdfPCell c2 = new PdfPCell(new Paragraph(" "
                    + e.getDate(), font(10, 0)));
            PdfPCell c3 = new PdfPCell(new Paragraph(
                    activeProfile.equals(ProfilesEnum.DZIESIATKA.getName())
                            || activeProfile.equals(ProfilesEnum.TEST.getName())
                            ? "Łódź" :
                            activeProfile.equals(ProfilesEnum.PANASZEW.getName())
                                    ? "Panaszew" : "",
                    font(10, 0)
            ));

            PdfPCell c4 = new PdfPCell(new Paragraph(pistol ? "X" : " ", font(10, 1)));
            PdfPCell c5 = new PdfPCell(new Paragraph(rifle ? "X" : " ", font(10, 1)));
            PdfPCell c6 = new PdfPCell(new Paragraph(shotgun ? "X" : " ", font(10, 1)));
            PdfPCell c7 = new PdfPCell(new Paragraph("WZSS", font(10, 0)));

            for (PdfPCell c : List.of(c1, c2, c3, c4, c5, c6, c7)) {
                c.setBorder(0);
                c.setFixedHeight(fixedHeight);
            }

            table.addCell(c1);
            table.addCell(c2);
            table.addCell(c3);
            table.addCell(c4);
            table.addCell(c5);
            table.addCell(c6);
            table.addCell(c7);

            document.add(table);

            if (counter == 4) {
                document.add(new Phrase("\n", font(3, 0)));
            }
        }
        return counter;
    }
}

