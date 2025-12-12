package com.shootingplace.shootingplace.file.csv.generator;

import com.shootingplace.shootingplace.file.csv.model.CsvGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@Component
public class MemberCsvGenerator {

    public CsvGenerationResults generate(MemberEntity memberEntity)
            throws IOException {

        String fileName = memberEntity.getFirstName().stripTrailing()
                + memberEntity.getSecondName().toUpperCase().stripTrailing()
                + ".csv";

        String[] tab = new String[5];

        LocalDate localDate = memberEntity.getBirthDate();
        String monthValue = String.valueOf(localDate.getMonthValue());
        if (Integer.parseInt(monthValue) < 10) {
            monthValue = "0" + monthValue;
        }
        String dayOfMonth = String.valueOf(localDate.getDayOfMonth());
        if (Integer.parseInt(dayOfMonth) < 10) {
            dayOfMonth = "0" + dayOfMonth;
        }
        String date = localDate.getYear() + "-" + monthValue + "-" + dayOfMonth;

        tab[0] = memberEntity.getPesel();
        tab[1] = memberEntity.getFirstName().trim();
        tab[2] = memberEntity.getSecondName().trim();
        tab[3] = date;
        tab[4] = memberEntity.getEmail();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String coma = ";";

        for (String s : tab) {
            baos.write((s + coma).getBytes());
        }

        return new CsvGenerationResults(fileName, baos.toByteArray());
    }
}
