package com.shootingplace.shootingplace.file.xlsx.generator;

import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Service
@RequiredArgsConstructor
public class ListOfErasedMembersXlsxGenerator {
    private final MemberRepository memberRepository;

    public XlsxGenerationResult generate(LocalDate firstDate, LocalDate secondDate) throws IOException {

        String fileName = "Lista osób skreślonych od " + firstDate + " do" + secondDate + ".pdf";
        List<MemberEntity> list = memberRepository.findAllByErasedTrue().stream().filter(f -> f.getErasedEntity() != null).filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)) && f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(MemberEntity::getFirstName, Collator.getInstance(Locale.forLanguageTag("pl")))).toList();
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return XlsxGenerationResult.builder().fileName(fileName).data(baos.toByteArray()).build();
    }
}
