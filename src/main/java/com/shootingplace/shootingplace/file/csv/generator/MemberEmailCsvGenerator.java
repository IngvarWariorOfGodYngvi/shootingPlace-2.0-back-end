package com.shootingplace.shootingplace.file.csv.generator;

import com.shootingplace.shootingplace.file.csv.model.CsvGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Component
public class MemberEmailCsvGenerator {

    private final MemberRepository memberRepository;

    public MemberEmailCsvGenerator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public CsvGenerationResults generate() throws IOException {

        List<MemberEntity> all = memberRepository.findAllByErasedFalse();
        String fileName = LocalDate.now() + " mailing.csv";

        StringBuilder csvContent = new StringBuilder();
        all.forEach(e -> csvContent.append(e.getEmail()).append(",").append("\n"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(csvContent.toString().getBytes());

        return new CsvGenerationResults(fileName, baos.toByteArray());
    }
}

