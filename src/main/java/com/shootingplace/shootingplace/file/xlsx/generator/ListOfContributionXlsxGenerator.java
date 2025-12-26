package com.shootingplace.shootingplace.file.xlsx.generator;

import com.shootingplace.shootingplace.contributions.Contribution;
import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import com.shootingplace.shootingplace.member.IMemberDTO;
import com.shootingplace.shootingplace.statistics.StatisticsService;
import com.shootingplace.shootingplace.wrappers.MemberWithContributionWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ListOfContributionXlsxGenerator {
    private final StatisticsService statisticsService;

    public XlsxGenerationResult generate(LocalDate firstDate, LocalDate secondDate) throws IOException {

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return XlsxGenerationResult.builder()
                .fileName(fileName)
                .data(baos.toByteArray())
                .build();
    }
}
