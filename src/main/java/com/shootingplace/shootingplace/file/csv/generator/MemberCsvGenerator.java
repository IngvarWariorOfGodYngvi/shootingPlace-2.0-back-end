package com.shootingplace.shootingplace.file.csv.generator;

import com.shootingplace.shootingplace.file.csv.model.CsvGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class MemberCsvGenerator {

    public CsvGenerationResults generate(MemberEntity memberEntity)
            throws IOException {

        String fileName = memberEntity.getFirstName().stripTrailing()
                + memberEntity.getSecondName().toUpperCase().stripTrailing()
                + ".xlsx";

        String[] tab = new String[5];

        tab[0] = memberEntity.getSecondName().trim();
        tab[1] = memberEntity.getFirstName().trim();
        tab[2] = "";
        tab[3] = memberEntity.getPesel();
        tab[4] = memberEntity.getEmail();
        tab[5] = memberEntity.getPhoneNumber();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String coma = ";";

        for (String s : tab) {
            baos.write((s + coma).getBytes());
        }

        return new CsvGenerationResults(fileName, baos.toByteArray());
    }
}
