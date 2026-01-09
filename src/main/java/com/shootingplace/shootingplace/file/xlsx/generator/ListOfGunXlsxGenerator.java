package com.shootingplace.shootingplace.file.xlsx.generator;

import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.armory.GunStoreEntity;
import com.shootingplace.shootingplace.armory.GunStoreRepository;
import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Service
@RequiredArgsConstructor
public class ListOfGunXlsxGenerator {
    private final GunStoreRepository gunStoreRepository;
    private final GunRepository gunRepository;

    public XlsxGenerationResult generate(List<String> guns) throws IOException {

        String fileName = "Lista_broni_w_magazynie_na_dzień" + LocalDate.now().format(dateFormat()) + ".xlsx";
        List<String> list = new ArrayList<>();
        for (int i = 0; i < guns.size(); i++) {
            int finalI = i;
            GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getUuid().equals(guns.get(finalI))).findFirst().orElseThrow(EntityNotFoundException::new);
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
            List<GunEntity> collect = gunRepository.findAll().stream().filter(f -> f.getGunType().equals(list.get(finalI))).filter(GunEntity::isInStock).sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName)).toList();
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return XlsxGenerationResult.builder().fileName(fileName).data(baos.toByteArray()).build();
    }
}
