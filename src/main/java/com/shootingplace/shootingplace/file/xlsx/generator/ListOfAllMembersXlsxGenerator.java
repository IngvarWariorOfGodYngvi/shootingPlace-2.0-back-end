package com.shootingplace.shootingplace.file.xlsx.generator;

import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Service
@RequiredArgsConstructor
public class ListOfAllMembersXlsxGenerator {

    private final MemberRepository memberRepository;

    public XlsxGenerationResult generate() throws IOException {

        String fileName = "Lista_klubowiczów_na_dzień " + LocalDate.now().format(dateFormat()) + ".xlsx";
        List<MemberEntity> collect = memberRepository.findAllByErasedFalse().stream().sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::isAdult)).toList();

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return XlsxGenerationResult.builder().fileName(fileName).data(baos.toByteArray()).build();
    }
}
