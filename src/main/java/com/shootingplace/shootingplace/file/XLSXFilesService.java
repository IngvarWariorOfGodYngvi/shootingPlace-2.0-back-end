package com.shootingplace.shootingplace.file;

import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.armory.GunStoreEntity;
import com.shootingplace.shootingplace.armory.GunStoreRepository;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.contributions.Contribution;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.member.IMemberDTO;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.statistics.StatisticsService;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.wrappers.MemberWithContributionWrapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class XLSXFilesService {


    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final FilesRepository filesRepository;
    private final MemberRepository memberRepository;
    private final GunStoreRepository gunStoreRepository;
    private final GunRepository gunRepository;
    private final Environment environment;
    private final StatisticsService statisticsService;

    private final Logger LOG = LogManager.getLogger(getClass());

    // rezultaty z zawodów
    public FilesEntity createAnnouncementInXLSXType(String tournamentUUID) throws IOException {
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
        cell1.setCellValue((environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź, " : "Panaszew, ") + dateFormat(tournamentEntity.getDate()));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        for (int i = 0; i < tournamentEntity.getCompetitionsList().size(); i++) {
            int cc = 0;
            if (!tournamentEntity.getCompetitionsList().get(i).getScoreList().isEmpty()) {
                CompetitionMembersListEntity competitionMembersListEntity = tournamentEntity.getCompetitionsList().get(i);

                List<ScoreEntity> scoreList;
                List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream()
                        .filter(f -> f.isDnf() || f.isDsq() || f.isPk())
                        .sorted(Comparator.comparing(ScoreEntity::getScore)
                                .thenComparing(ScoreEntity::getInnerTen)
                                .thenComparing(ScoreEntity::getOuterTen).reversed())
                        .toList();

                if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.TIME.getName())) {
                    scoreList = competitionMembersListEntity.getScoreList().stream()
                            .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk())
                            .sorted(Comparator.comparing(ScoreEntity::getScore)
                                    .thenComparing(ScoreEntity::getInnerTen)
                                    .thenComparing(ScoreEntity::getOuterTen))
                            .collect(Collectors.toList());
                } else {
                    scoreList = competitionMembersListEntity.getScoreList().stream()
                            .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk())
                            .sorted(Comparator.comparing(ScoreEntity::getScore)
                                    .thenComparing(ScoreEntity::getInnerTen)
                                    .thenComparing(ScoreEntity::getOuterTen).reversed())
                            .collect(Collectors.toList());
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
                    if (countingMethod.equals(CountingMethod.COMSTOCK.getName()) ||
                            countingMethod.equals(CountingMethod.TIME.getName()) ||
                            countingMethod.equals(CountingMethod.IPSC.getName()) ||
                            countingMethod.equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {
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
            mainArbiterClass = tournamentEntity.getMainArbiter().getMemberPermissions().getArbiterClass();
            mainArbiterClass = getArbiterClass(mainArbiterClass);
        } else {
            if (tournamentEntity.getOtherMainArbiter() != null) {
                mainArbiter = tournamentEntity.getOtherMainArbiter().getFirstName() + " " + tournamentEntity.getOtherMainArbiter().getSecondName();
                mainArbiterClass = tournamentEntity.getOtherMainArbiter().getPermissionsEntity().getArbiterClass();
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
            arbiterRTSClass = tournamentEntity.getCommissionRTSArbiter().getMemberPermissions().getArbiterClass();
            arbiterRTSClass = getArbiterClass(arbiterRTSClass);
        } else {
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                arbiterRTS = tournamentEntity.getOtherCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getOtherCommissionRTSArbiter().getSecondName();
                arbiterRTSClass = tournamentEntity.getOtherCommissionRTSArbiter().getPermissionsEntity().getArbiterClass();
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

        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);
        FilesEntity filesEntity = createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    // lista składek w wybranym okresie
    public FilesEntity getContributions(LocalDate firstDate, LocalDate secondDate) throws IOException {
        String fileName = "Lista_składek_od_" + firstDate + "_do_" + secondDate + ".xlsx";
        List<MemberWithContributionWrapper> collect = statisticsService.getContributionSum(firstDate, secondDate);

        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet("lista");

        XSSFFont fontNormal = workbook.createFont();
        fontNormal.setFontHeightInPoints((short) 10);
        fontNormal.setFontName("Calibri");
        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();

        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");
        cellStyleTitle.setFont(fontTitle);
        cellStyleTitle.setAlignment(HorizontalAlignment.CENTER);
        cellStyleTitle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFCellStyle cellStyleNormalCenterAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormalCenterAlignment.setFont(fontNormal);

        XSSFCellStyle cellStyleNormalLeftAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.LEFT);
        cellStyleNormalCenterAlignment.setFont(fontNormal);

        XSSFRow row2 = sheet.createRow(rc++);
        XSSFCell cell7 = row2.createCell(0);
        cell7.setCellValue("Lista zapisów od " + firstDate + " do " + secondDate);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        cell7.setCellStyle(cellStyleTitle);
        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);

        cell.setCellValue("lp");
        cell1.setCellValue("Nazwisko i Imię");
        cell2.setCellValue("Numer Legitymacji");
        cell3.setCellValue("Data Składki");

        cell.setCellStyle(cellStyleNormalCenterAlignment);
        cell1.setCellStyle(cellStyleNormalCenterAlignment);
        cell2.setCellStyle(cellStyleNormalCenterAlignment);
        cell3.setCellStyle(cellStyleNormalCenterAlignment);


        for (int i = 0; i < collect.size(); i++) {
            IMemberDTO member = collect.get(i).getMember();
            Contribution contribution = collect.get(i).getContribution();
            int cc = 0;
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(cc++);
            cell1 = row1.createCell(cc++);
            cell2 = row1.createCell(cc++);
            cell3 = row1.createCell(cc);

            cell.setCellStyle(cellStyleNormalCenterAlignment);
            cell1.setCellStyle(cellStyleNormalLeftAlignment);
            cell2.setCellStyle(cellStyleNormalCenterAlignment);
            cell3.setCellStyle(cellStyleNormalCenterAlignment);

            cell.setCellValue(i + 1);
            cell1.setCellValue(member.getSecond_name().replaceAll(" ", "") + ' ' +
                    member.getFirst_name().replaceAll(" ", ""));
            cell2.setCellValue(member.getLegitimation_number());
            cell3.setCellValue(contribution.getPaymentDay().toString());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);
        FilesEntity filesEntity = createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    // lista zapisów w wybranym okresie
    public FilesEntity getJoinDateSum(LocalDate firstDate, LocalDate secondDate) throws IOException {
        String fileName = "lista klubowiczów zapisanych od " + firstDate.toString() + " do " + secondDate.toString() + ".xlsx";

        List<MemberDTO> collect = memberRepository.findAll().stream()
                .filter(f -> f.getJoinDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getJoinDate().isBefore(secondDate.plusDays(1)))
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getJoinDate).thenComparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName))
                .toList();

        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet("lista");

        XSSFFont fontNormal = workbook.createFont();
        fontNormal.setFontHeightInPoints((short) 10);
        fontNormal.setFontName("Calibri");
        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();

        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");
        cellStyleTitle.setFont(fontTitle);
        cellStyleTitle.setAlignment(HorizontalAlignment.CENTER);
        cellStyleTitle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFCellStyle cellStyleNormalCenterAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormalCenterAlignment.setFont(fontNormal);

        XSSFCellStyle cellStyleNormalLeftAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.LEFT);
        cellStyleNormalCenterAlignment.setFont(fontNormal);

        XSSFRow row2 = sheet.createRow(rc++);
        XSSFCell cell7 = row2.createCell(0);
        cell7.setCellValue("Lista zapisów od " + firstDate + " do " + secondDate);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        cell7.setCellStyle(cellStyleTitle);
        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);

        cell.setCellValue("lp");
        cell1.setCellValue("Nazwisko i Imię");
        cell2.setCellValue("Numer Legitymacji");
        cell3.setCellValue("Data Zapisu");

        cell.setCellStyle(cellStyleNormalCenterAlignment);
        cell1.setCellStyle(cellStyleNormalCenterAlignment);
        cell2.setCellStyle(cellStyleNormalCenterAlignment);
        cell3.setCellStyle(cellStyleNormalCenterAlignment);


        for (int i = 0; i < collect.size(); i++) {
            MemberDTO memberDTO = collect.get(i);
            int cc = 0;
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(cc++);
            cell1 = row1.createCell(cc++);
            cell2 = row1.createCell(cc++);
            cell3 = row1.createCell(cc);

            cell.setCellStyle(cellStyleNormalCenterAlignment);
            cell1.setCellStyle(cellStyleNormalLeftAlignment);
            cell2.setCellStyle(cellStyleNormalCenterAlignment);
            cell3.setCellStyle(cellStyleNormalCenterAlignment);

            cell.setCellValue(i + 1);
            cell1.setCellValue(memberDTO.getFullName());
            cell2.setCellValue(memberDTO.getLegitimationNumber());
            cell3.setCellValue(memberDTO.getJoinDate().toString());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);
        FilesEntity filesEntity = createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    // lista skreślonych w wybranym okresie
    public FilesEntity getErasedSum(LocalDate firstDate, LocalDate secondDate) throws IOException {
        String fileName = "lista klubowiczów skreślonych od " + firstDate.toString() + " do " + secondDate.toString() + ".xlsx";

        List<MemberDTO> collect = memberRepository.findAllByErasedTrue().stream()
                .filter(f -> f.getErasedEntity() != null)
                .filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1)))
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getJoinDate).thenComparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName))
                .toList();

        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet("lista");

        XSSFFont fontNormal = workbook.createFont();
        fontNormal.setFontHeightInPoints((short) 10);
        fontNormal.setFontName("Calibri");
        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();

        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");
        cellStyleTitle.setFont(fontTitle);
        cellStyleTitle.setAlignment(HorizontalAlignment.CENTER);
        cellStyleTitle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFCellStyle cellStyleNormalCenterAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormalCenterAlignment.setFont(fontNormal);

        XSSFCellStyle cellStyleNormalLeftAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.LEFT);
        cellStyleNormalCenterAlignment.setFont(fontNormal);

        XSSFRow row2 = sheet.createRow(rc++);
        XSSFCell cell7 = row2.createCell(0);
        cell7.setCellValue("Lista zapisów od " + firstDate + " do " + secondDate);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        cell7.setCellStyle(cellStyleTitle);
        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);

        cell.setCellValue("lp");
        cell1.setCellValue("Nazwisko i Imię");
        cell2.setCellValue("Numer Legitymacji");
        cell3.setCellValue("Data Skreślenia");

        cell.setCellStyle(cellStyleNormalCenterAlignment);
        cell1.setCellStyle(cellStyleNormalLeftAlignment);
        cell2.setCellStyle(cellStyleNormalCenterAlignment);
        cell3.setCellStyle(cellStyleNormalCenterAlignment);


        for (int i = 0; i < collect.size(); i++) {
            MemberDTO memberDTO = collect.get(i);
            int cc = 0;
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(cc++);
            cell1 = row1.createCell(cc++);
            cell2 = row1.createCell(cc++);
            cell3 = row1.createCell(cc);

            cell.setCellStyle(cellStyleNormalCenterAlignment);
            cell1.setCellStyle(cellStyleNormalCenterAlignment);
            cell2.setCellStyle(cellStyleNormalCenterAlignment);
            cell3.setCellStyle(cellStyleNormalCenterAlignment);

            cell.setCellValue(i + 1);
            cell1.setCellValue(memberDTO.getFullName());
            cell2.setCellValue(memberDTO.getLegitimationNumber());
            cell3.setCellValue(memberDTO.getErasedEntity().getDate().toString());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);
        FilesEntity filesEntity = createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    public FilesEntity getAllMembersToTableXLSXFile(boolean condition) throws IOException {

        String fileName = "Lista_klubowiczów_na_dzień " + LocalDate.now().format(dateFormat()) + ".xlsx";

        List<MemberEntity> collect = memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> f.isAdult() == condition)
                .sorted(Comparator.comparing(MemberEntity::getSecondName, pl())).toList();


        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Lista klubowiczów");

        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);
        XSSFCell cell4 = row.createCell(4);
        XSSFCell cell5 = row.createCell(5);
        XSSFCell cell6 = row.createCell(6);


        cell.setCellValue("lp");
        cell1.setCellValue("Nazwisko i Imię");
        cell2.setCellValue("Nr. legitymacji");
        cell3.setCellValue("Data urodzenia");
        cell4.setCellValue("Data zapisu");
        cell5.setCellValue("Data opłacenia składki");
        cell6.setCellValue("Składka ważna do");
        sheet.createRow(rc++);

        for (int i = 0; i < collect.size(); i++) {
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(0);
            cell1 = row1.createCell(1);
            cell2 = row1.createCell(2);
            cell3 = row1.createCell(3);
            cell4 = row1.createCell(4);
            cell5 = row1.createCell(5);
            cell6 = row1.createCell(6);

            cell.setCellValue(i + 1);
            cell1.setCellValue(collect.get(i).getFullName());
            cell2.setCellValue(collect.get(i).getLegitimationNumber());
            cell3.setCellValue(collect.get(i).getBirthDate().format(dateFormat()));
            cell4.setCellValue(collect.get(i).getJoinDate().toString());

            if (!collect.get(i).getHistory().getContributionList().isEmpty()) {

                cell5.setCellValue(collect.get(i).getHistory().getContributionList().getFirst().getPaymentDay().toString());
                cell6.setCellValue(collect.get(i).getHistory().getContributionList().getFirst().getValidThru().toString());

            } else {

                cell5.setCellValue("BRAK SKŁADEK");
                cell6.setCellValue("BRAK SKŁADEK");

            }
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);
        sheet.autoSizeColumn(6);
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);
        FilesEntity filesEntity = createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    public FilesEntity getAllMembersWithLicenseXlsx() throws IOException {
        String fileName = "Lista_klubowiczów_na_dzień_z_licencją_do_Katowic " + LocalDate.now().format(dateFormat()) + ".xlsx";
        List<MemberEntity> collect = memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> f.getClub().getId() == 1)
                .filter(f -> f.getLicense() != null && f.getLicense().getNumber() != null)
                .sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl()))
                .toList();


        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Lista osób z licencją");

        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell0 = row.createCell(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        cell0.setCellValue("Załącznik nr 1");
        XSSFRow row1 = sheet.createRow(rc++);

        XSSFCell cell01 = row1.createCell(0);
        XSSFCell cell11;
        XSSFCell cell21;
        XSSFCell cell31;
        XSSFCell cell41;
        XSSFCell cell51;

        sheet.addMergedRegion(new CellRangeAddress(rc - 1, rc - 1, 0, 5));
        cell01.setCellValue("Wykaz członków Ligi Obrony Kraju Klubu: " + collect.getFirst().getClub().getFullName());
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFont(fontTitle);
        cell01.setCellStyle(cellStyle);
        XSSFRow row2 = sheet.createRow(rc++);

        XSSFCell cell02 = row2.createCell(0);
        XSSFCell cell12 = row2.createCell(1);
        XSSFCell cell22 = row2.createCell(2);
        XSSFCell cell32 = row2.createCell(3);
        XSSFCell cell42 = row2.createCell(4);
        XSSFCell cell52 = row2.createCell(5);

        cell02.setCellValue("lp.");
        cell12.setCellValue("Nazwisko i Imię");
        cell22.setCellValue("PESEL");
        cell32.setCellValue("Adres zamieszkania");
        cell42.setCellValue("Klub");
        cell52.setCellValue("Numer Licencji");

        for (int i = 0; i < collect.size(); i++) {

            XSSFRow row3 = sheet.createRow(rc++);
            cell01 = row3.createCell(0);
            cell11 = row3.createCell(1);
            cell21 = row3.createCell(2);
            cell31 = row3.createCell(3);
            cell41 = row3.createCell(4);
            cell51 = row3.createCell(5);

            cell01.setCellValue(i + 1);
            XSSFCellStyle cellStyle1 = workbook.createCellStyle();
            cellStyle1.setAlignment(HorizontalAlignment.LEFT);
            cell01.setCellStyle(cellStyle1);
            cell11.setCellValue(collect.get(i).getFullName());
            cell21.setCellValue(collect.get(i).getPesel());
            cell31.setCellValue(collect.get(i).getAddress().fullAddress());
            cell41.setCellValue(collect.get(i).getClub().getShortName());
            cell51.setCellValue(collect.get(i).getLicense().getNumber());
            XSSFCellStyle cellStyle2 = workbook.createCellStyle();
            cellStyle2.setAlignment(HorizontalAlignment.CENTER);
            cell51.setCellStyle(cellStyle2);

        }
        sheet.autoSizeColumn(0);    // lp
        sheet.autoSizeColumn(1);    // Imię i nazwisko
        sheet.autoSizeColumn(2);    // PESEL
        sheet.autoSizeColumn(3);    // Adres
        sheet.autoSizeColumn(4);    // Klub (KS DZIESIĄTKA)
        sheet.autoSizeColumn(5);    // Numer Licencji

        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);
        FilesEntity filesEntity = createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    public FilesEntity getGunRegistryXlsx(List<String> guns) throws IOException {

        String fileName = "Lista_broni_w_magazynie_na_dzień" + LocalDate.now().format(dateFormat()) + ".xlsx";


        List<String> list = new ArrayList<>();

        for (int i = 0; i < guns.size(); i++) {
            int finalI = i;
            GunStoreEntity gunStoreEntity = gunStoreRepository.findAll()
                    .stream()
                    .filter(f -> f.getUuid().equals(guns.get(finalI)))
                    .findFirst().orElseThrow(EntityNotFoundException::new);
            if (!gunStoreEntity.getGunEntityList().isEmpty()) {
                list.add(gunStoreEntity.getTypeName());
            }
            list.sort(String::compareTo);
        }

        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Lista klubowiczów");

        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");
        cellStyleTitle.setFont(fontTitle);
        cellStyleTitle.setAlignment(HorizontalAlignment.CENTER);
        cellStyleTitle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);
        XSSFCell cell4 = row.createCell(4);
        XSSFCell cell5 = row.createCell(5);
        XSSFCell cell6 = row.createCell(6);
        XSSFCell cell7 = row.createCell(7);

        cell.setCellValue("lp");
        cell1.setCellValue("Marka i Model");
        cell2.setCellValue("Kaliber i rok produkcji");
        cell3.setCellValue("Numer i seria");
        cell4.setCellValue("Poz. z książki ewidencji");
        cell5.setCellValue("Magazynki");
        cell6.setCellValue("Numer świadectwa");
        cell7.setCellValue("Data Wpisu");

        for (int i = 0; i < list.size(); i++) {
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(0);
            cell.setCellValue(list.get(i));
            sheet.addMergedRegion(new CellRangeAddress(rc - 1, rc - 1, 0, 6));
            cell.setCellStyle(cellStyleTitle);

            int finalI = i;
            List<GunEntity> collect = gunRepository.findAll()
                    .stream()
                    .filter(f -> f.getGunType().equals(list.get(finalI)))
                    .filter(GunEntity::isInStock)
                    .sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName))
                    .toList();
            if (!collect.isEmpty()) {

                for (int j = 0; j < collect.size(); j++) {
                    GunEntity gun = collect.get(j);

                    XSSFRow row2 = sheet.createRow(rc++);
                    cell = row2.createCell(0);
                    cell1 = row2.createCell(1);
                    cell2 = row2.createCell(2);
                    cell3 = row2.createCell(3);
                    cell4 = row2.createCell(4);
                    cell5 = row2.createCell(5);
                    cell6 = row2.createCell(6);
                    cell7 = row2.createCell(7);

                    cell.setCellValue(j + 1);
                    cell1.setCellValue(gun.getModelName());
                    String caliberAndProductionYearGun;


                    if (gun.getProductionYear() != null && !gun.getProductionYear().isEmpty() && !gun.getProductionYear().equals("null")) {
                        caliberAndProductionYearGun = gun.getCaliber() + " rok " + gun.getProductionYear();
                    } else {
                        caliberAndProductionYearGun = gun.getCaliber();
                    }

                    cell2.setCellValue(caliberAndProductionYearGun);
                    cell3.setCellValue(gun.getSerialNumber());
                    cell4.setCellValue(gun.getRecordInEvidenceBook());
                    cell5.setCellValue(gun.getNumberOfMagazines());
                    cell6.setCellValue(gun.getGunCertificateSerialNumber());
                    cell7.setCellValue(gun.getAddedDate());

                }
                sheet.createRow(rc++);
            }
        }

        sheet.autoSizeColumn(0);//lp
        sheet.autoSizeColumn(1);//Marka i Model
        sheet.autoSizeColumn(2);//Kaliber i rok produkcji
        sheet.autoSizeColumn(3);//Numer i seria
        sheet.autoSizeColumn(4);//Poz. z książki ewidencji
        sheet.autoSizeColumn(5);//Magazynki
        sheet.autoSizeColumn(6);//Numer świadectwa
        sheet.autoSizeColumn(7);//Data wpisu

        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    public FilesEntity getAllErasedMembersXlsx(LocalDate firstDate, LocalDate secondDate) throws IOException {

        String fileName = "Lista osób skreślonych od " + firstDate + " do" + secondDate + ".pdf";
        List<MemberEntity> list = memberRepository.findAllByErasedTrue().stream()
                .filter(f -> f.getErasedEntity() != null)
                .filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)) && f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1)))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(MemberEntity::getFirstName, Collator.getInstance(Locale.forLanguageTag("pl"))))
                .toList();
        int rc = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Lista osób skreślonych od " + firstDate.format(dateFormat()) + " do " + secondDate.format(dateFormat()) + ".xlsx");

        XSSFCellStyle cellStyleNormal = workbook.createCellStyle();
        cellStyleNormal.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormal.setWrapText(true);
        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();

        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");
        cellStyleTitle.setFont(fontTitle);
        cellStyleTitle.setAlignment(HorizontalAlignment.CENTER);
        cellStyleTitle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFRow row2 = sheet.createRow(rc++);
        XSSFCell cellTitle = row2.createCell(0);
        cellTitle.setCellValue("Lista osób skreślonych od " + firstDate.format(dateFormat()) + " do " + secondDate.format(dateFormat()));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        cellTitle.setCellStyle(cellStyleTitle);

        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);
        XSSFCell cell4 = row.createCell(4);
        XSSFCell cell5 = row.createCell(5);
        cell.setCellStyle(cellStyleNormal);
        cell2.setCellStyle(cellStyleNormal);
        cell3.setCellStyle(cellStyleNormal);
        cell4.setCellStyle(cellStyleNormal);
        cell5.setCellStyle(cellStyleNormal);

        cell.setCellValue("lp");
        cell1.setCellValue("Nazwisko i Imię");
        cell2.setCellValue("Numer Legitymacji");
        cell3.setCellValue("PESEL");
        cell4.setCellValue("Przyczyna skreślenia");
        cell5.setCellValue("Informacje dodatkowe");

        for (int i = 0; i < list.size(); i++) {

            MemberEntity member = list.get(i);
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(0);
            cell1 = row1.createCell(1);
            cell2 = row1.createCell(2);
            cell3 = row1.createCell(3);
            cell4 = row1.createCell(4);
            cell5 = row1.createCell(5);

            cell.setCellValue(i + 1);
            cell1.setCellValue(member.getFullName());
            cell2.setCellValue(member.getLegitimationNumber());
            cell3.setCellValue(member.getPesel());
            cell4.setCellValue(member.getErasedEntity().getErasedType() + "\n" + member.getErasedEntity().getDate());
            cell5.setCellValue(member.getErasedEntity().getAdditionalDescription());

            cell.setCellStyle(cellStyleNormal);
            cell1.setCellStyle(cellStyleNormal);
            cell2.setCellStyle(cellStyleNormal);
            cell3.setCellStyle(cellStyleNormal);
            cell4.setCellStyle(cellStyleNormal);
            cell5.setCellStyle(cellStyleNormal);
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);


        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    public FilesEntity generateAllMembersToErasedListXlsx() throws IOException {

        String fileName = "Lista osób do skreślelnia.xlsx";
        LocalDate notValidDate = LocalDate.now().minusMonths(6);
        List<MemberEntity> list = memberRepository.findAllByErasedFalseAndActiveFalse().stream()
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().getFirst().getValidThru().minusDays(1).isBefore(notValidDate))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))).toList();
        int rc = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Lista osób do skreślenia");

        XSSFCellStyle cellStyleNormal = workbook.createCellStyle();
        cellStyleNormal.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormal.setWrapText(true);
        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFFont fontTitle = workbook.createFont();

        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");
        cellStyleTitle.setFont(fontTitle);
        cellStyleTitle.setAlignment(HorizontalAlignment.CENTER);
        cellStyleTitle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFRow row2 = sheet.createRow(rc++);
        XSSFCell cellTitle = row2.createCell(0);
        cellTitle.setCellValue("Lista osób do skreślenia");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        cellTitle.setCellStyle(cellStyleTitle);

        XSSFRow row = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);
        XSSFCell cell4 = row.createCell(4);
        XSSFCell cell5 = row.createCell(5);
        cell.setCellStyle(cellStyleNormal);
        cell2.setCellStyle(cellStyleNormal);
        cell3.setCellStyle(cellStyleNormal);
        cell4.setCellStyle(cellStyleNormal);
        cell5.setCellStyle(cellStyleNormal);

        cell.setCellValue("lp");
        cell1.setCellValue("Nazwisko i Imię");
        cell2.setCellValue("Numer Legitymacji");
        cell3.setCellValue("PESEL");
        cell4.setCellValue("");
        cell5.setCellValue("");

        for (int i = 0; i < list.size(); i++) {

            MemberEntity member = list.get(i);
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(0);
            cell1 = row1.createCell(1);
            cell2 = row1.createCell(2);
            cell3 = row1.createCell(3);
            cell4 = row1.createCell(4);
            cell5 = row1.createCell(5);

            cell.setCellValue(i + 1);
            cell1.setCellValue(member.getFullName());
            cell2.setCellValue(member.getLegitimationNumber());
            cell3.setCellValue(member.getPesel());
            cell4.setCellValue("");
            cell5.setCellValue("");

            cell.setCellStyle(cellStyleNormal);
            cell1.setCellStyle(cellStyleNormal);
            cell2.setCellStyle(cellStyleNormal);
            cell3.setCellStyle(cellStyleNormal);
            cell4.setCellStyle(cellStyleNormal);
            cell5.setCellStyle(cellStyleNormal);
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);


        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    public FilesEntity getCSV() throws IOException {
        String fileName = "database.csv";

        String CSV_HEADER = "numer legitymacji,imię i nazwisko,numer licencji,data składki,P,K,S\n";

        List<MemberEntity> all = memberRepository.findAllByErasedFalse();
        StringBuilder csvContent = new StringBuilder();
        csvContent.append(CSV_HEADER);

        all.forEach(e -> csvContent.append(e.getLegitimationNumber()).append(",")
                .append(e.getFullName()).append(",")
                .append(e.getLicense().getNumber()).append(",")
                .append(e.getHistory().getContributionList().getFirst().getValidThru()).append(",")
                .append(e.getHistory().getPistolCounter()).append(",")
                .append(e.getHistory().getRifleCounter()).append(",")
                .append(e.getHistory().getShotgunCounter()).append("\n"));
        FileWriter fw = new FileWriter(fileName);
        try {
            fw.write(String.valueOf(csvContent));
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        fw.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = getFilesModel(fileName, data);

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik {}", fileName);

        return filesEntity;
    }

    private byte[] convertToByteArray(String path) throws IOException {
        File file = new File(path);
        return Files.readAllBytes(file.toPath());

    }

    FilesEntity createFileEntity(FilesModel filesModel) {
        filesModel.setDate(LocalDate.now());
        filesModel.setTime(LocalTime.now());
        FilesEntity filesEntity = Mapping.map(filesModel);
        return filesRepository.save(filesEntity);

    }

    private FilesModel getFilesModel(String fileName, byte[] data) {
        return FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();
    }

    private String dateFormat(LocalDate date) {

        String day = String.valueOf(date.getDayOfMonth());
        String month = "";

        if (date.getMonth().getValue() == 1) {
            month = "Stycznia";
        }
        if (date.getMonth().getValue() == 2) {
            month = "Lutego";
        }
        if (date.getMonth().getValue() == 3) {
            month = "Marca";
        }
        if (date.getMonth().getValue() == 4) {
            month = "Kwietnia";
        }
        if (date.getMonth().getValue() == 5) {
            month = "Maja";
        }
        if (date.getMonth().getValue() == 6) {
            month = "Czerwca";
        }
        if (date.getMonth().getValue() == 7) {
            month = "Lipca";
        }
        if (date.getMonth().getValue() == 8) {
            month = "Sierpnia";
        }
        if (date.getMonth().getValue() == 9) {
            month = "Września";
        }
        if (date.getMonth().getValue() == 10) {
            month = "Października";
        }
        if (date.getMonth().getValue() == 11) {
            month = "Listopada";
        }
        if (date.getMonth().getValue() == 12) {
            month = "Grudnia";
        }
        String year = String.valueOf(date.getYear());


        return day + " " + month + " " + year;


    }

    @NotNull
    private String getArbiterClass(String arbiterClass) {
        switch (arbiterClass) {
            case "Klasa 3":
                arbiterClass = "Sędzia Klasy Trzeciej";
                break;
            case "Klasa 2":
                arbiterClass = "Sędzia Klasy Drugiej";
                break;
            case "Klasa 1":
                arbiterClass = "Sędzia Klasy Pierwszej";
                break;
            case "Klasa Państwowa":
                arbiterClass = "Sędzia Klasy Państwowej";
                break;
            case "Klasa Międzynarodowa":
                arbiterClass = "Sędzia Klasy Międzynarodowej";
                break;
            default:
                LOG.info("Nie znaleziono Klasy Sędziowskiej");

        }
        return arbiterClass;
    }

    private Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    private DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }

    private String arabicToRomanNumberConverter(int arabicNumber) {
        return switch (arabicNumber) {
            case 0 -> "";
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> "error";
        };
    }

}
