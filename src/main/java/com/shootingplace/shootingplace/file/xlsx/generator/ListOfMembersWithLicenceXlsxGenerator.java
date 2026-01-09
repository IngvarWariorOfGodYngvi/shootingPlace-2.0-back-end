package com.shootingplace.shootingplace.file.xlsx.generator;

import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Service
@RequiredArgsConstructor
public class ListOfMembersWithLicenceXlsxGenerator {
    private final MemberRepository memberRepository;

    public XlsxGenerationResult generate(

    ) throws IOException {
        String fileName = "Lista_klubowiczów_na_dzień_z_licencją_do_Katowic " + LocalDate.now().format(dateFormat()) + ".xlsx";
        List<MemberEntity> collect = memberRepository.findAllByErasedFalse().stream().filter(f -> f.getClub().getId() == 1).filter(f -> f.getLicense() != null && f.getLicense().getNumber() != null).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).toList();


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


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return XlsxGenerationResult.builder().fileName(fileName).data(baos.toByteArray()).build();
    }
}
