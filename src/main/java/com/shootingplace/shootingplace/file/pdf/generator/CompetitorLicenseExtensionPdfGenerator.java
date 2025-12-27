package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.history.CompetitionHistoryEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
@RequiredArgsConstructor
public class CompetitorLicenseExtensionPdfGenerator implements PdfGenerator<MemberEntity> {

    private final Environment environment;

    @Override
    public PdfGenerationResults generate(MemberEntity member) throws DocumentException, IOException {

        String fileName = "Wniosek " + member.getFullName() + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(35F, 35F, 50F, 50F);

        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, true, PageStampMode.A4));

        document.open();

        PdfContentByte cb = writer.getDirectContent();
        PdfReader reader = new PdfReader("Wniosek_o_przedluzenie_licencji_zawodniczej.pdf");
        cb.addTemplate(writer.getImportedPage(reader, 1), 0, 0);

        String licenseNumber = member.getLicense().getNumber();
        char[] pesel = member.getPesel().toCharArray();

        String name = member.getSecondName().toUpperCase() + "  " + member.getFirstName().toUpperCase();

        String phone = member.getPhoneNumber();
        String phoneSplit = phone.substring(0, 3) + " " + phone.substring(3, 6) + " " + phone.substring(6, 9) + " " + phone.substring(9, 12);

        String yearShort = member.getLicense().getValidThru().toString().substring(2, 4);

        int licenceYear = member.getLicense().getValidThru().getYear();

        List<CompetitionHistoryEntity> history = member.getHistory().getCompetitionHistory();


        List<CompetitionHistoryEntity> pistolStarts = collectByDiscipline(history, licenceYear, Discipline.PISTOL);

        List<CompetitionHistoryEntity> rifleStarts = collectByDiscipline(history, licenceYear, Discipline.RIFLE);

        List<CompetitionHistoryEntity> shotgunStarts = collectByDiscipline(history, licenceYear, Discipline.SHOTGUN);

        int pistol = member.getShootingPatent().isPistolPermission() ? pistolStarts.size() : 0;

        int rifle = member.getShootingPatent().isRiflePermission() ? rifleStarts.size() : 0;

        int shotgun = member.getShootingPatent().isShotgunPermission() ? shotgunStarts.size() : 0;

        if (pistol >= 4) {
            pistol = 4;
            rifle = Math.min(rifle, 2);
            shotgun = Math.min(shotgun, 2);
        } else if (rifle >= 4) {
            rifle = 4;
            pistol = Math.min(pistol, 2);
            shotgun = Math.min(shotgun, 2);
        } else if (shotgun >= 4) {
            shotgun = 4;
            pistol = Math.min(pistol, 2);
            rifle = Math.min(rifle, 2);
        }

        Paragraph patentNumber = new Paragraph(member.getShootingPatent().getPatentNumber() + "                                                       " + licenseNumber, font(12, 0));
        patentNumber.setIndentationLeft(160);

        Paragraph peselParagraph = new Paragraph(pesel[0] + "   " + pesel[1] + "   " + pesel[2] + "   " + pesel[3] + "   " + pesel[4] + "   " + pesel[5] + "  " + pesel[6] + "   " + pesel[7] + "   " + pesel[8] + "   " + pesel[9] + "   " + pesel[10] + "                                             " + phoneSplit, font(12, 0));
        peselParagraph.setIndentationLeft(72);

        Paragraph names = new Paragraph(name, font(12, 0));
        names.setIndentationLeft(150);

        Paragraph yearParagraph = new Paragraph(yearShort, font(12, 1));
        yearParagraph.setIndentationLeft(350);

        for (int i = 0; i < 11; i++) {
            document.add(new Paragraph("\n", font(7, 0)));
        }

        document.add(patentNumber);
        document.add(new Paragraph("\n", font(7, 0)));
        document.add(peselParagraph);
        document.add(new Paragraph("\n", font(7, 0)));
        document.add(names);

        for (int i = 0; i < 4; i++) {
            document.add(new Paragraph("\n", font(9, 0)));
        }

        document.add(yearParagraph);

        for (int i = 0; i < 4; i++) {
            document.add(new Paragraph("\n", font(8, 0)));
        }

        ClubEntity club = member.getClub();
        ProfilesEnum profile = ProfilesEnum.fromName(environment.getActiveProfiles()[0]);

        int counter = 0;
        float fixedHeight = 27F;

        counter = addDisciplineRows(document, pistolStarts, pistol, club, profile, counter, fixedHeight, new float[]{50, 20, 20, 5, 10, 2, 28}, true, false, false);

        counter = addDisciplineRows(document, rifleStarts, rifle, club, profile, counter, fixedHeight, new float[]{50, 20, 20, 5, 2, 10, 28}, false, true, false);

        addDisciplineRows(document, shotgunStarts, shotgun, club, profile, counter, fixedHeight, new float[]{50, 20, 20, 8, 3, 6, 28}, false, false, true);

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private List<CompetitionHistoryEntity> collectByDiscipline(List<CompetitionHistoryEntity> history, int year, Discipline discipline) {
        return history.stream().filter(h -> h.getDate() != null).filter(h -> h.getDate().getYear() == year).filter(h -> h.getDisciplineList() != null).filter(h -> h.getDisciplineList().stream().anyMatch(d -> d.equals(discipline.getName()))).toList();
    }

    private int addDisciplineRows(Document document, List<CompetitionHistoryEntity> list, int limit, ClubEntity club, ProfilesEnum profile, int counter, float fixedHeight, float[] widths, boolean pistol, boolean rifle, boolean shotgun) throws DocumentException, IOException {

        for (int i = 0; i < limit; i++) {

            PdfPTable table = new PdfPTable(widths);
            CompetitionHistoryEntity e = list.get(i);
            counter++;

            String city = switch (profile) {
                case TEST, DZIESIATKA -> "Łódź";
                case PANASZEW -> "Panaszew";
                case GUARDIANS -> "Nowolipsk";
                case MECHANIK -> "Tomaszów \n Mazowiecki";
            };

            PdfPCell c1 = new PdfPCell(new Paragraph(e.getName() + "\n" + club.getShortName(), font(11, 0)));
            PdfPCell c2 = new PdfPCell(new Paragraph(" " + e.getDate(), font(11, 0)));
            PdfPCell c3 = new PdfPCell(new Paragraph(city, font(11, 0)));
            PdfPCell c4 = new PdfPCell(new Paragraph(pistol ? "X" : " ", font(11, 1)));
            PdfPCell c5 = new PdfPCell(new Paragraph(rifle ? "X" : " ", font(11, 1)));
            PdfPCell c6 = new PdfPCell(new Paragraph(shotgun ? "X" : " ", font(11, 1)));
            PdfPCell c7 = new PdfPCell(new Paragraph("WZSS", font(11, 0)));

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
                document.add(new Phrase("\n", font(2, 0)));
            }
        }
        return counter;
    }
}
