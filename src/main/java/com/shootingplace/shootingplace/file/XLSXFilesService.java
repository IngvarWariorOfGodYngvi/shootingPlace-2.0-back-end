package com.shootingplace.shootingplace.file;

import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.file.xlsx.generator.*;
import com.shootingplace.shootingplace.file.xlsx.model.XlsxGenerationResult;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XLSXFilesService {

    private final FilesRepository filesRepository;
    private final MemberRepository memberRepository;
    private final TournamentResultsXlsxGenerator tournamentResultsXlsxGenerator;
    private final ListOfContributionXlsxGenerator listOfContributionXlsxGenerator;
    private final ListOfSignUpsXlsxGenerator listOfSignUpsXlsxGenerator;
    private final ListOfErasedXlsxGenerator listOfErasedXlsxGenerator;
    private final ListOfAllMembersXlsxGenerator listOfAllMembersXlsxGenerator;
    private final ListOfMembersWithLicenceXlsxGenerator listOfMembersWithLicenceXlsxGenerator;
    private final ListOfGunXlsxGenerator listOfGunXlsxGenerator;
    private final ListOfErasedMembersXlsxGenerator listOfErasedMembersXlsxGenerator;
    private final ListOfMembersToEraseXlsxGenerator listOfMembersToEraseXlsxGenerator;
    private final MemberXlsxGenerator memberXlsxGenerator;
    private final Logger LOG = LogManager.getLogger(getClass());

    // rezultaty z zawodów
    public FilesEntity createAnnouncementInXLSXType(String tournamentUUID) throws IOException {
        XlsxGenerationResult xlsx = tournamentResultsXlsxGenerator.generate(tournamentUUID);
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista składek w wybranym okresie
    public FilesEntity getContributions(LocalDate firstDate, LocalDate secondDate) throws IOException {
        XlsxGenerationResult xlsx = listOfContributionXlsxGenerator.generate(firstDate, secondDate);
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista zapisów w wybranym okresie
    public FilesEntity getJoinDateSum(LocalDate firstDate, LocalDate secondDate) throws IOException {
        XlsxGenerationResult xlsx = listOfSignUpsXlsxGenerator.generate(firstDate, secondDate);
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista skreślonych w wybranym okresie
    public FilesEntity getErasedSum(LocalDate firstDate, LocalDate secondDate) throws IOException {
        XlsxGenerationResult xlsx = listOfErasedXlsxGenerator.generate(firstDate, secondDate);
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista wszystkich klubowiczów
    public FilesEntity getAllMembersToTableXLSXFile() throws IOException {
        XlsxGenerationResult xlsx = listOfAllMembersXlsxGenerator.generate();
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista klubowiczów z licencją do Katowic
    public FilesEntity getAllMembersWithLicenseXlsx() throws IOException {
        XlsxGenerationResult xlsx = listOfMembersWithLicenceXlsxGenerator.generate();
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista broni w magazynie
    public FilesEntity getGunRegistryXlsx(List<String> guns) throws IOException {
        XlsxGenerationResult xlsx = listOfGunXlsxGenerator.generate(guns);
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista osób skreślonych
    public FilesEntity getAllErasedMembersXlsx(LocalDate firstDate, LocalDate secondDate) throws IOException {
        XlsxGenerationResult xlsx = listOfErasedMembersXlsxGenerator.generate(firstDate, secondDate);
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // lista osób do skreślenia
    public FilesEntity generateAllMembersToErasedListXlsx() throws IOException {
        XlsxGenerationResult xlsx = listOfMembersToEraseXlsxGenerator.generate();
        return createFile(xlsx.getFileName(), xlsx.getData());
    }

    // plik .xlsx klubowicza
    public FilesEntity getMemberXLSXFile(String memberUUID) throws IOException {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(() -> new DomainNotFoundException("Member", memberUUID));
        XlsxGenerationResult xlsx = memberXlsxGenerator.generate(memberEntity);
        return createFile(xlsx.getFileName(), xlsx.getData());
    }
    // potrzebne do eksportu na SOZ
    public byte[] generateMemberXlsxForSoz(String memberUUID) throws IOException {
        MemberEntity memberEntity = memberRepository.findById(memberUUID)
                .orElseThrow(() -> new DomainNotFoundException("Member", memberUUID));

        XlsxGenerationResult xlsx = memberXlsxGenerator.generate(memberEntity);
        createFile(xlsx.getFileName(), xlsx.getData());
        return xlsx.getData();
    }
    public FilesEntity createFile(String fileName, byte[] data) {
        FilesModel model = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .date(LocalDate.now())
                .time(LocalTime.now())
                .size(data.length).build();
        return createFileEntity(model);
    }

    private FilesEntity createFileEntity(FilesModel filesModel) {
        filesModel.setDate(LocalDate.now());
        filesModel.setTime(LocalTime.now());
        FilesEntity filesEntity = Mapping.map(filesModel);
        LOG.info("{} Encja została zapisana", filesModel.getName().trim());
        return filesRepository.save(filesEntity);

    }
}
