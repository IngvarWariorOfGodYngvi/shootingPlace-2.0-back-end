package com.shootingplace.shootingplace.file.xlsx.generator;

import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import com.shootingplace.shootingplace.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
@Service
@RequiredArgsConstructor
public class MemberXlsxGenerator {

    public XlsxGenerationResult generate(MemberEntity member) throws IOException {

        String fileName = "member_" + member.getSecondName() + "_" + member.getFirstName() + ".xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("dane");

        // ===== FONT =====
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontName("Calibri");
        headerFont.setFontHeightInPoints((short) 11);

        XSSFFont normalFont = workbook.createFont();
        normalFont.setFontName("Calibri");
        normalFont.setFontHeightInPoints((short) 11);

        // ===== STYLE =====
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        XSSFCellStyle normalStyle = workbook.createCellStyle();
        normalStyle.setFont(normalFont);

        int rc = 0;

        // ===== HEADER =====
        XSSFRow headerRow = sheet.createRow(rc++);

        String[] headers = {
                "Nazwisko",
                "Imię",
                "Drugie imię",
                "PESEL",
                "Email",
                "Telefon"
        };

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ===== DATA =====
        XSSFRow dataRow = sheet.createRow(rc);

        XSSFCell cell;

        cell = dataRow.createCell(0);
        cell.setCellValue(member.getSecondName());
        cell.setCellStyle(normalStyle);

        cell = dataRow.createCell(1);
        cell.setCellValue(member.getFirstName());
        cell.setCellStyle(normalStyle);

        cell = dataRow.createCell(2);
        cell.setCellValue(""); // może być null
        cell.setCellStyle(normalStyle);

        cell = dataRow.createCell(3);
        cell.setCellValue(member.getPesel());
        cell.setCellStyle(normalStyle);

        cell = dataRow.createCell(4);
        cell.setCellValue(member.getEmail());
        cell.setCellStyle(normalStyle);

        cell = dataRow.createCell(5);
        cell.setCellValue(member.getPhoneNumber());
        cell.setCellStyle(normalStyle);

        // ===== AUTOSIZE =====
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return XlsxGenerationResult.builder().fileName(fileName).data(baos.toByteArray()).build();

    }
}
