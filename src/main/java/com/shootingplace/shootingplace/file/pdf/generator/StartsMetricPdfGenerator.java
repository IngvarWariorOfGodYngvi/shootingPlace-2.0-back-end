package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.competition.CompetitionEntity;
import com.shootingplace.shootingplace.competition.CompetitionRepository;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
@RequiredArgsConstructor
public class StartsMetricPdfGenerator {

    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final TournamentRepository tournamentRepository;
    private final CompetitionRepository competitionRepository;
    private final ClubRepository clubRepository;
    private final Environment environment;

    public PdfGenerationResults generate(String memberUUID, String otherID, String tournamentUUID, List<String> competitions, String startNumber, Boolean a5rotate) throws IOException, DocumentException {

        String name;
        String club;

        if (otherID != null && !otherID.isEmpty()) {
            OtherPersonEntity otherPersonEntity = otherPersonRepository.findById(Integer.parseInt(otherID)).orElseThrow(EntityNotFoundException::new);
            name = otherPersonEntity.getSecondName().toUpperCase(Locale.ROOT) + " " + otherPersonEntity.getFirstName();
            club = otherPersonEntity.getClub().getShortName();
        } else {
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
            name = memberEntity.getSecondName().toUpperCase(Locale.ROOT) + " " + memberEntity.getFirstName();
            club = memberEntity.getClub().getShortName();
        }

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity clubEntity = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);

        String fileName = "metryki_" + name + ".pdf";

        a5rotate = a5rotate != null && a5rotate;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(a5rotate ? PageSize.A5.rotate() : PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, true, a5rotate ? PageStampMode.A5_LANDSCAPE : PageStampMode.A4));

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<String> comp = competitions.stream().map(m -> competitionRepository.findById(m).orElseThrow(EntityNotFoundException::new).getName()).filter(value -> !value.contains(" pneumatyczny ") && !value.contains(" pneumatyczna ")).sorted().collect(Collectors.toList());

        // dopisz pneumatyczne na końcu
        competitions.stream().map(m -> competitionRepository.findById(m).orElseThrow(EntityNotFoundException::new).getName()).filter(competition -> competition.contains(" pneumatyczny ") || competition.contains(" pneumatyczna ")).sorted().forEach(comp::add);

        for (int j = 0; j < comp.size(); j++) {
            int d = Integer.parseInt(startNumber);
            int finalJ = j;

            ScoreEntity score = tournamentEntity.getCompetitionsList().stream().filter(f -> f.getName().equals(comp.get(finalJ))).findFirst().orElseThrow(EntityNotFoundException::new).getScoreList().stream().filter(f -> f.getMetricNumber() == d).findFirst().orElseThrow(EntityNotFoundException::new);

            CompetitionEntity competitionEntity = competitionRepository.findByNameEquals(comp.get(finalJ)).orElseThrow(EntityNotFoundException::new);

            int numberOfShots;
            if (competitionEntity.getNumberOfShots() > 10) {
                numberOfShots = 10;
            } else {
                numberOfShots = competitionEntity.getNumberOfShots();
            }

            // nazwa zawodów, tytuł i data
            Paragraph par1 = new Paragraph(tournamentEntity.getName().toUpperCase() + "    " + clubEntity.getShortName() + " " + tournamentEntity.getDate().format(dateFormat()), font(11, 1));
            par1.setAlignment(1);

            String a = "";
            String b = "";
            if (score.isAmmunition()) {
                a = "A";
                b = "";
            }
            if (!score.isAmmunition() && score.isGun()) {
                b = "B";
            }

            // nazwisko klub i numer startowy
            Chunk nameChunk = new Chunk(name, font(10, 1));
            Chunk clubChunk = new Chunk(" " + club, font(10, 1));
            Chunk numberChunk = new Chunk(a + " " + b + "  Nr. " + startNumber, font(13, 1));

            // nazwa konkurencji
            Paragraph par3 = new Paragraph(comp.get(j), font(10, 1));
            par3.setAlignment(1);

            Paragraph par4 = new Paragraph("Podpis sędziego .............................", font(11, 0));
            Chunk chunk1 = new Chunk("                                   Podpis zawodnika .............................       ", font(11, 0));
            Chunk chunk2 = new Chunk(" Nr. " + startNumber, font(13, 1));
            par4.add(chunk1);
            par4.add(chunk2);

            int numberOfColumns = numberOfShots + 5;
            float[] pointColumnWidths = new float[numberOfColumns];
            if (!competitionEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                pointColumnWidths = new float[6];
            }
            if (competitionEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                for (int i = 0; i < numberOfColumns; i++) {
                    if (i < numberOfColumns - 2) {
                        pointColumnWidths[i] = 20F;
                    } else {
                        pointColumnWidths[i] = 30F;
                    }
                }
            } else {
                Arrays.fill(pointColumnWidths, 25F);
            }

            PdfPTable table = new PdfPTable(pointColumnWidths);
            PdfPTable table1 = new PdfPTable(pointColumnWidths);
            PdfPTable table11 = new PdfPTable(pointColumnWidths);

            float[] t2points = new float[5];
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
                t2points = new float[]{25F, 25F, 10F, 10F, 10F};
            }
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                t2points = new float[]{33F, 33F, 33F, 0F, 0F};
            }
            PdfPTable table2 = new PdfPTable(t2points);

            table.setWidthPercentage(100F);
            table1.setWidthPercentage(100F);
            table11.setWidthPercentage(100F);
            table2.setWidthPercentage(80F);

            Paragraph p0t2 = new Paragraph(nameChunk);
            Paragraph p1t2 = new Paragraph(clubChunk);
            Paragraph p2t2 = new Paragraph(numberChunk);
            Paragraph p3t2 = new Paragraph("BW", font(8, 0));
            Paragraph p4t2 = new Paragraph("BK", font(8, 0));
            Paragraph p5t2 = new Paragraph(" ", font(12, 0));
            Paragraph p6t2 = new Paragraph(" ", font(12, 0));
            Paragraph p7t2 = new Paragraph(" ", font(12, 0));
            Paragraph p8t2 = new Paragraph(" ", font(12, 0));
            Paragraph p9t2 = new Paragraph(" ", font(12, 0));

            p0t2.setAlignment(0);
            p1t2.setAlignment(0);
            p2t2.setAlignment(0);
            p3t2.setAlignment(0);
            p4t2.setAlignment(0);

            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                p3t2 = new Paragraph(" ", font(10, 0));
                p4t2 = new Paragraph(" ", font(10, 0));
            }

            PdfPCell cell0t2 = new PdfPCell(p0t2);
            PdfPCell cell1t2 = new PdfPCell(p1t2);
            PdfPCell cell2t2 = new PdfPCell(p2t2);
            PdfPCell cell3t2 = new PdfPCell(p3t2);
            PdfPCell cell4t2 = new PdfPCell(p4t2);
            PdfPCell cell5t2 = new PdfPCell(p5t2);
            PdfPCell cell6t2 = new PdfPCell(p6t2);
            PdfPCell cell7t2 = new PdfPCell(p7t2);
            PdfPCell cell8t2 = new PdfPCell(p8t2);
            PdfPCell cell9t2 = new PdfPCell(p9t2);

            cell0t2.setBorderWidth(0);
            cell1t2.setBorderWidth(0);
            cell2t2.setBorderWidth(0);
            cell5t2.setBorderWidth(0);
            cell6t2.setBorderWidth(0);
            cell7t2.setBorderWidth(0);

            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                cell3t2.setBorderWidth(0);
                cell4t2.setBorderWidth(0);
                cell8t2.setBorderWidth(0);
                cell9t2.setBorderWidth(0);
            }

            table2.addCell(cell0t2);
            table2.addCell(cell1t2);
            table2.addCell(cell2t2);
            table2.addCell(cell3t2);
            table2.addCell(cell4t2);
            table2.addCell(cell5t2);
            table2.addCell(cell6t2);
            table2.addCell(cell7t2);
            table2.addCell(cell8t2);
            table2.addCell(cell9t2);

            document.add(par1); //  nazwa zawodów i tytuł
            document.add(new Paragraph("\n", font(4, 0))); // nowa linia
            document.add(table2); // nazwisko klub i numer startowy
            document.add(par3); // nazwa konkurencji
            document.add(new Paragraph("\n", font(4, 0))); // nowa linia

            if (competitionEntity.getCountingMethod().equals(CountingMethod.POJEDYNEK.getName())) {
                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p;
                    if (i == 4 || i == 5) {
                        p = new Paragraph("UWAGI", font(12, 0));
                    } else {
                        p = new Paragraph(" ", font(12, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                }
                document.add(table); // tytuł tabeli

                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph(" ", font(28, 0));
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table1.addCell(cell);
                }
                document.add(table1); // ciało tabeli
            }

            if (competitionEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                for (int i = 0; i < numberOfColumns; i++) {
                    Paragraph p = new Paragraph(String.valueOf(i), font(12, 0));
                    if (i == 0) p = new Paragraph("seria", font(12, 0));
                    if (i == numberOfColumns - 4) p = new Paragraph("zw", font(12, 0));
                    if (i == numberOfColumns - 3) p = new Paragraph("wew", font(12, 0));
                    if (i == numberOfColumns - 2) p = new Paragraph("SUMA", font(12, 0));
                    if (i == numberOfColumns - 1) p = new Paragraph("UWAGI", font(12, 0));
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                    if (i == numberOfColumns - 1) {
                        document.add(table);
                        break;
                    }
                }

                if (numberOfShots < 10) {
                    for (int i = 0; i < numberOfColumns; i++) {
                        Chunk c = new Chunk(" ", font(26, 0));
                        Paragraph p = new Paragraph(c);
                        PdfPCell cell = new PdfPCell(p);
                        table11.addCell(cell);
                        if (i == numberOfColumns - 1) {
                            document.add(table11);
                            break;
                        }
                    }
                }

                int serial = 0;
                int numberOfRows = (competitionEntity.getNumberOfShots() / 10) + 1;
                for (int i = 0; i < numberOfRows; i++) {
                    for (int k = 0; k < numberOfColumns; k++) {
                        String s = " ";
                        if (i < numberOfRows - 1) {
                            if (k % numberOfColumns == 0) {
                                s = arabicToRomanNumberConverter(++serial);
                            }
                            Chunk c;
                            if (k % numberOfColumns == 0) {
                                c = new Chunk(s, font(10, 0));
                            } else {
                                c = new Chunk(s, font(20, 0));
                            }
                            Paragraph p = new Paragraph(c);
                            PdfPCell cell = new PdfPCell(p);
                            p.setAlignment(1);
                            cell.setHorizontalAlignment(1);
                            cell.setVerticalAlignment(1);
                            table1.addCell(cell);
                        } else {
                            Paragraph p = new Paragraph(s, font(20, 0));
                            PdfPCell cell = new PdfPCell(p);
                            if (k < numberOfColumns - 2) cell.setBorder(0);
                            if (k == numberOfColumns - 1) cell.setBorder(0);
                            table1.addCell(cell);
                        }
                    }
                }
                document.add(table1);
            }

            if (competitionEntity.getCountingMethod().equals(CountingMethod.DYNAMIKADZIESIATKA.getName()) || competitionEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName()) || competitionEntity.getCountingMethod().equals(CountingMethod.IPSC.getName()) || competitionEntity.getCountingMethod().equals(CountingMethod.TIME.getName())) {

                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph();
                    if (i == 0) p = new Paragraph("ALFA", font(12, 0));
                    if (i == 1) p = new Paragraph("CHARLIE", font(12, 0));
                    if (i == 2) p = new Paragraph("DELTA", font(12, 0));
                    if (i == 3) p = new Paragraph("PROCEDURY", font(12, 0));
                    if (i == 4) p = new Paragraph("CZAS", font(12, 0));
                    if (i == 5) p = new Paragraph("UWAGI", font(12, 0));
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                }
                document.add(table); // tytuł tabeli

                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph(" ", font(28, 0));
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table1.addCell(cell);
                }
                document.add(table1); // ciało tabeli
            }

            Paragraph par5 = new Paragraph("_______________________________________________________________________________________", font(12, 0));
            document.add(par4);
            document.add(par5);

            if ((j + 1) % 4 == 0) {
                document.newPage();
            }
            if (comp.size() > 1 && a5rotate) {
                if (j < comp.size() - 1) {
                    document.newPage();
                }
            }
        }

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private static String arabicToRomanNumberConverter(int number) {
        final String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};
        if (number >= 0 && number < romans.length) return romans[number];
        return Integer.toString(number);
    }

}
