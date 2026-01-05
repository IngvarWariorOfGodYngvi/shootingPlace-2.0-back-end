package com.shootingplace.shootingplace.file.xlsx.generator;

import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.shootingplace.shootingplace.file.utils.Utils.arabicToRomanNumberConverter;
import static com.shootingplace.shootingplace.file.utils.Utils.getArbiterClass;

@Service
@RequiredArgsConstructor
public class TournamentResultsXlsxGenerator {

    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;

    public XlsxGenerationResult generate(String tournamentUUID) throws IOException {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity c = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);
        int rc = 0;

        String fileName = "rezultaty" + c.getShortName().toUpperCase() + ".xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();

        //style
        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFCellStyle cellStyleDate = workbook.createCellStyle();
        XSSFCellStyle cellStyleCompetitionTitle = workbook.createCellStyle();
        XSSFCellStyle cellStyleCompetitionSubTitle = workbook.createCellStyle();
        XSSFCellStyle cellStyleNormalCenterAlignment = workbook.createCellStyle();

        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");

        XSSFFont fontDate = workbook.createFont();
        fontDate.setItalic(true);
        fontDate.setFontHeightInPoints((short) 11);
        fontDate.setFontName("Calibri");

        XSSFFont fontCompetitionTitle = workbook.createFont();
        fontCompetitionTitle.setBold(true);
        fontCompetitionTitle.setFontHeightInPoints((short) 12);
        fontCompetitionTitle.setFontName("Calibri");

        XSSFFont fontCompetitionSubTitle = workbook.createFont();
        fontCompetitionSubTitle.setBold(true);
        fontCompetitionSubTitle.setFontHeightInPoints((short) 10);
        fontCompetitionSubTitle.setFontName("Calibri");

        XSSFFont fontNormalCenterAlignment = workbook.createFont();
        fontNormalCenterAlignment.setFontHeightInPoints((short) 10);
        fontNormalCenterAlignment.setFontName("Calibri");

        cellStyleTitle.setFont(fontTitle);

        cellStyleDate.setFont(fontDate);

        cellStyleCompetitionTitle.setFont(fontCompetitionTitle);

        cellStyleCompetitionSubTitle.setFont(fontCompetitionSubTitle);
        cellStyleCompetitionSubTitle.setAlignment(HorizontalAlignment.CENTER);

        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormalCenterAlignment.setFont(fontNormalCenterAlignment);

        XSSFSheet sheet = workbook.createSheet("rezultaty-" + tournamentEntity.getDate());

        XSSFRow row = sheet.createRow(rc++);

        XSSFRow row1 = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(cellStyleTitle);

        XSSFCell cell1 = row1.createCell(0);
        cell1.setCellStyle(cellStyleDate);

        sheet.setColumnWidth(0, 11 * 128);
        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(3, 25 * 256);
        for (int i = 4; i < 13; i++) {
            sheet.setColumnWidth(i, 18 * 128);
        }
        cell.setCellValue(tournamentEntity.getName().toUpperCase() + " " + c.getShortName());
        cell1.setCellValue(c.getCity());
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        for (int i = 0; i < tournamentEntity.getCompetitionsList().size(); i++) {
            int cc = 0;
            if (!tournamentEntity.getCompetitionsList().get(i).getScoreList().isEmpty()) {
                CompetitionMembersListEntity competitionMembersListEntity = tournamentEntity.getCompetitionsList().get(i);

                List<ScoreEntity> scoreList;
                List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDnf() || f.isDsq() || f.isPk()).sorted(Comparator.comparing(ScoreEntity::getScore).thenComparing(ScoreEntity::getInnerTen).thenComparing(ScoreEntity::getOuterTen).reversed()).toList();

                if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.TIME.getName())) {
                    scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk()).sorted(Comparator.comparing(ScoreEntity::getScore).thenComparing(ScoreEntity::getInnerTen).thenComparing(ScoreEntity::getOuterTen)).collect(Collectors.toList());
                } else {
                    scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk()).sorted(Comparator.comparing(ScoreEntity::getScore).thenComparing(ScoreEntity::getInnerTen).thenComparing(ScoreEntity::getOuterTen).reversed()).collect(Collectors.toList());
                }

                scoreList.addAll(collect);
                XSSFRow row2 = sheet.createRow(rc);
                XSSFCell cell2 = row2.createCell(cc);

                cell2.setCellStyle(cellStyleCompetitionTitle);

                cell2.setCellValue(competitionMembersListEntity.getName());
                sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 0, 6));
                XSSFRow row3 = sheet.createRow(rc);
                XSSFCell cell31 = row3.createCell(cc++); // m-ce
                XSSFCell cell32 = row3.createCell(cc++); // nazwisko i imię
                XSSFCell cell33 = row3.createCell(cc++);
                XSSFCell cell34 = row3.createCell(cc++); // klub
                List<XSSFCell> series = new ArrayList<>();
                if (competitionMembersListEntity.getScoreList().getFirst().getSeries().size() > 1 && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                    for (int k = 0; k < competitionMembersListEntity.getScoreList().getFirst().getSeries().size(); k++) {
                        series.add(row3.createCell(cc++));
                    }
                }
                XSSFCell cell36 = row3.createCell(cc++); // 10X
                XSSFCell cell37 = row3.createCell(cc++); // 10/
                XSSFCell cell35 = row3.createCell(cc); // wynik

                cell31.setCellValue("M-ce");
                cell32.setCellValue("Nazwisko i Imię");
                cell33.setCellValue("");
                cell34.setCellValue("Klub");
                if (competitionMembersListEntity.getScoreList().getFirst().getSeries().size() > 1 && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                    for (int k = 0; k < series.size(); k++) {
                        series.get(k).setCellValue("Seria " + arabicToRomanNumberConverter(k + 1));
                        series.get(k).setCellStyle(cellStyleCompetitionSubTitle);
                    }
                }
                if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                    cell35.setCellValue("Wynik");
                }
                if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {
                    cell35.setCellValue("Wynik");
                }
                if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.IPSC.getName())) {
                    cell35.setCellValue("Wynik");
                }
                if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                    cell35.setCellValue("Wynik");
                }
                if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.TIME.getName())) {
                    cell35.setCellValue("Czas");
                }
                cell31.setCellStyle(cellStyleCompetitionSubTitle);
                cell32.setCellStyle(cellStyleCompetitionSubTitle);
                cell33.setCellStyle(cellStyleCompetitionSubTitle);
                cell34.setCellStyle(cellStyleCompetitionSubTitle);
                cell35.setCellStyle(cellStyleCompetitionSubTitle);
                cell36.setCellStyle(cellStyleCompetitionSubTitle);
                cell37.setCellStyle(cellStyleCompetitionSubTitle);

                sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 1, 2));

                if (competitionMembersListEntity.getCountingMethod() != null) {
                    if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                        cell36.setCellValue("czas");
                        cell37.setCellValue("procedury");
                    }
                    if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.IPSC.getName())) {
                        cell36.setCellValue("czas");
                        cell37.setCellValue("procedury");
                    }
                    if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {
                        cell36.setCellValue("czas");
                        cell37.setCellValue("procedury");
                    }
                    if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.TIME.getName())) {
                        cell36.setCellValue("");
                        cell37.setCellValue("procedury");
                    }
                    if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {

                        if (competitionMembersListEntity.getName().toLowerCase(Locale.ROOT).contains("karabin") && competitionMembersListEntity.getName().toLowerCase(Locale.ROOT).contains("pneumatyczny")) {
                            cell36.setCellValue("");
                            cell37.setCellValue("10 /");
                        } else {
                            cell36.setCellValue("10 x");
                            cell37.setCellValue("10 /");
                        }
                    }
                }

                for (int j = 0; j < scoreList.size(); j++) {

                    String secondName;
                    String firstName;
                    String club;
                    if (scoreList.get(j).getMember() != null) {
                        secondName = scoreList.get(j).getMember().getSecondName();
                        firstName = scoreList.get(j).getMember().getFirstName();
                        club = scoreList.get(j).getMember().getClub().getShortName();

                    } else {
                        secondName = scoreList.get(j).getOtherPersonEntity().getSecondName();
                        firstName = scoreList.get(j).getOtherPersonEntity().getFirstName();
                        club = scoreList.get(j).getOtherPersonEntity().getClub().getShortName();

                    }
                    float score = scoreList.get(j).getScore();
                    boolean afterComa = scoreList.stream().anyMatch(f -> {
                        String s = String.format("%.1f", f.getScore());
                        return Integer.parseInt(s.substring(s.indexOf(",") + 1)) > 0;
                    });
                    String countingMethod = competitionMembersListEntity.getCountingMethod();
                    String scoreOuterTen;
                    String scoreInnerTen = String.format("%.2f", scoreList.get(j).getInnerTen());
                    if (countingMethod.equals(CountingMethod.COMSTOCK.getName()) || countingMethod.equals(CountingMethod.IPSC.getName()) || countingMethod.equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {
                        scoreOuterTen = String.format("%.4f", scoreList.get(j).getOuterTen());
                    } else {
                        scoreInnerTen = String.format("%.0f", scoreList.get(j).getInnerTen());
                        scoreOuterTen = String.format("%.0f", scoreList.get(j).getOuterTen());
                    }
                    String procedures = String.valueOf(scoreList.get(j).getProcedures());
                    if (scoreOuterTen.startsWith("0")) {
                        scoreOuterTen = "";
                    }
                    if (scoreInnerTen.startsWith("0")) {
                        scoreInnerTen = "";
                    }
                    String o1 = scoreInnerTen, o2 = scoreOuterTen;
//                            scoreInnerTen.replace(".0", ""), o2 = scoreOuterTen.replace(".0", "");
                    if (scoreList.get(j).getInnerTen() == 0) {
                        o1 = scoreInnerTen = "";
                    }
                    if (scoreList.get(j).getOuterTen() == 0) {
                        o2 = scoreOuterTen = "";
                    }
                    String result;
                    if (countingMethod.equals(CountingMethod.COMSTOCK.getName()) || countingMethod.equals(CountingMethod.TIME.getName()) || countingMethod.equals(CountingMethod.IPSC.getName()) || countingMethod.equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {
                        if (countingMethod.equals(CountingMethod.TIME.getName())) {
                            result = String.format("%.2f", score);
                        } else {
                            result = String.format("%.4f", score);
                        }
                    } else {
                        if (afterComa && countingMethod.equals(CountingMethod.NORMAL.getName())) {
                            result = String.format("%.1f", score);
                        } else {
                            result = String.valueOf(Math.round(score));
                        }
                    }
                    if (scoreList.get(j).isDnf()) {
                        result = "DNF";
                    }
                    if (scoreList.get(j).isDsq()) {
                        result = "DSQ";
                    }
                    if (scoreList.get(j).isPk()) {
                        result = result.concat("(PK)");
                    }
                    if (competitionMembersListEntity.getCountingMethod() != null) {

                        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                            o2 = procedures.replace(".0", "");
                        }
                        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.IPSC.getName())) {
                            o2 = procedures.replace(".0", "");
                        }
                        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {
                            o2 = procedures.replace(".0", "");
                        }
                        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.TIME.getName())) {
                            o2 = procedures.replace(".0", "");
                        }
                        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                            o1 = scoreInnerTen.replace(".0", "");
                            o2 = scoreOuterTen.replace(".0", "");
                        }
                    }

                    XSSFRow row4 = sheet.createRow(rc);
                    cc = 0;
                    XSSFCell cell41 = row4.createCell(cc++); //M-ce
                    XSSFCell cell42 = row4.createCell(cc++); //Imię i nazwisko
                    XSSFCell cell43 = row4.createCell(cc++); //Imię i nazwisko
                    XSSFCell cell44 = row4.createCell(cc++); //Klub
                    List<XSSFCell> series1 = new ArrayList<>();
                    if (scoreList.get(j).getSeries().size() > 1 && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                        for (int k = 0; k < competitionMembersListEntity.getScoreList().getFirst().getSeries().size(); k++) {
                            series1.add(row4.createCell(cc++));
                        }
                    }
                    XSSFCell cell46 = row4.createCell(cc++); //10x
                    XSSFCell cell47 = row4.createCell(cc++); //10/

                    XSSFCell cell45 = row4.createCell(cc); //Wynik

                    cell41.setCellValue(String.valueOf(j + 1));
                    cell42.setCellValue(secondName + " " + firstName);
                    cell43.setCellValue("");
                    cell44.setCellValue(club);
                    if (scoreList.get(j).getSeries().size() > 1 && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                        for (int k = 0; k < series1.size(); k++) {
                            series1.get(k).setCellValue(String.format("%.1f", scoreList.get(j).getSeries().get(k)));
                            series1.get(k).setCellStyle(cellStyleNormalCenterAlignment);
                        }
                    }
                    cell45.setCellValue(result);
                    cell46.setCellValue(o1);
                    cell47.setCellValue(o2);
                    cell46.setCellStyle(cellStyleNormalCenterAlignment);
                    cell47.setCellStyle(cellStyleNormalCenterAlignment);
                    cell41.setCellStyle(cellStyleNormalCenterAlignment);
                    cell45.setCellStyle(cellStyleCompetitionSubTitle);

                    sheet.addMergedRegion(new CellRangeAddress(rc, rc, 1, 2));
                    if (j < scoreList.size() - 1) {
                        rc++;
                    }
                }
                rc++;
            }


        }

        rc++;

        XSSFRow row2 = sheet.createRow(rc);
        sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 1, 3));
        XSSFRow row3 = sheet.createRow(rc);
        sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 1, 3));
        XSSFRow row4 = sheet.createRow(rc);
        sheet.addMergedRegion(new CellRangeAddress(rc, rc, 1, 3));

        XSSFCell cell2 = row2.createCell(1);
        XSSFCell cell3 = row3.createCell(1);
        XSSFCell cell4 = row4.createCell(1);

        cell2.setCellValue("Zawody odbyły się zgodnie z przepisami bezpieczeństwa");
        cell3.setCellValue("i regulaminem zawodów, oraz liczba sklasyfikowanych zawodników");
        cell4.setCellValue("była zgodna ze stanem faktycznym.");
        rc++;
        String mainArbiter;
        String mainArbiterClass;
        if (tournamentEntity.getMainArbiter() != null) {
            mainArbiter = tournamentEntity.getMainArbiter().getFirstName() + " " + tournamentEntity.getMainArbiter().getSecondName();
            mainArbiterClass = tournamentEntity.getMainArbiter().getMemberPermissions().getArbiterStaticClass();
            mainArbiterClass = getArbiterClass(mainArbiterClass);
        } else {
            if (tournamentEntity.getOtherMainArbiter() != null) {
                mainArbiter = tournamentEntity.getOtherMainArbiter().getFirstName() + " " + tournamentEntity.getOtherMainArbiter().getSecondName();
                mainArbiterClass = tournamentEntity.getOtherMainArbiter().getPermissionsEntity().getArbiterStaticClass();
                mainArbiterClass = getArbiterClass(mainArbiterClass);
            } else {
                mainArbiter = "Nie Wskazano";
                mainArbiterClass = "";
            }
        }

        String arbiterRTS;
        String arbiterRTSClass;
        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            arbiterRTS = tournamentEntity.getCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getCommissionRTSArbiter().getSecondName();
            arbiterRTSClass = tournamentEntity.getCommissionRTSArbiter().getMemberPermissions().getArbiterStaticClass();
            arbiterRTSClass = getArbiterClass(arbiterRTSClass);
        } else {
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                arbiterRTS = tournamentEntity.getOtherCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getOtherCommissionRTSArbiter().getSecondName();
                arbiterRTSClass = tournamentEntity.getOtherCommissionRTSArbiter().getPermissionsEntity().getArbiterStaticClass();
                arbiterRTSClass = getArbiterClass(arbiterRTSClass);
            } else {
                arbiterRTS = "Nie Wskazano";
                arbiterRTSClass = "";
            }
        }

        rc++;
        XSSFRow row5 = sheet.createRow(rc++);
        XSSFRow row6 = sheet.createRow(rc++);
        XSSFRow row7 = sheet.createRow(rc);

        XSSFCell cell5 = row5.createCell(1);
        XSSFCell cell6 = row6.createCell(1);
        XSSFCell cell7 = row7.createCell(1);

        XSSFCell cell51 = row5.createCell(3);
        XSSFCell cell61 = row6.createCell(3);
        XSSFCell cell71 = row7.createCell(3);


        cell5.setCellValue("Sędzia Główny");
        cell6.setCellValue(mainArbiter);
        cell7.setCellValue(mainArbiterClass);

        cell51.setCellValue("Przewodniczący Komisji RTS");
        cell61.setCellValue(arbiterRTS);
        cell71.setCellValue(arbiterRTSClass);

        cell5.setCellStyle(cellStyleNormalCenterAlignment);
        cell51.setCellStyle(cellStyleNormalCenterAlignment);

        cell6.setCellStyle(cellStyleCompetitionSubTitle);
        cell61.setCellStyle(cellStyleCompetitionSubTitle);

        cell7.setCellStyle(cellStyleNormalCenterAlignment);
        cell71.setCellStyle(cellStyleNormalCenterAlignment);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return XlsxGenerationResult.builder().fileName(fileName).data(baos.toByteArray()).build();
    }
}

