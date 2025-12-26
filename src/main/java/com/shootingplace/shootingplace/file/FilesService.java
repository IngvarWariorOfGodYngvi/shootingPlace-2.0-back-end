package com.shootingplace.shootingplace.file;

import com.google.common.hash.Hashing;
import com.lowagie.text.DocumentException;
import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.csv.generator.MemberCsvGenerator;
import com.shootingplace.shootingplace.file.csv.generator.MemberEmailCsvGenerator;
import com.shootingplace.shootingplace.file.csv.model.CsvGenerationResults;
import com.shootingplace.shootingplace.file.pdf.generator.*;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import com.shootingplace.shootingplace.otherPerson.OtherPerson;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonService;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.wrappers.ImageOtherPersonWrapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilesService {

    private final MemberRepository memberRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final FilesRepository filesRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final GunRepository gunRepository;
    private final OtherPersonService otherPersonService;
    private final UserRepository userRepository;
    private final Environment environment;
    private final Logger LOG = LogManager.getLogger(getClass());

    private final LokMembershipPdfGenerator lokMembershipPdfGenerator;
    private final PersonalCardPdfGenerator personalCardPdfGenerator;
    private final GuardiansMembershipPdfGenerator guardiansMembershipPdfGenerator;
    private final ContributionConfirmPdfGenerator contributionConfirmPdfGenerator;
    private final AmmunitionListPdfGenerator ammunitionListPdfGenerator;
    private final CompetitorLicenseExtensionPdfGenerator competitorLicenseExtensionPdfGenerator;
    private final CertificateOfClubMembershipPanaszewPdfGenerator certificateOfClubMembershipPanaszewPdfGenerator;
    private final CertificateOfClubMembershipDziesiatkaPdfGenerator certificateOfClubMembershipDziesiatkaPdfGenerator;
    private final ApplicationForFirearmsLicensePdfGenerator applicationForFirearmsLicensePdfGenerator;
    private final MemberCsvGenerator memberCsvGenerator;
    private final MemberEmailCsvGenerator memberEmailCsvGenerator;
    private final StartsMetricPdfGenerator startsMetricPdfGenerator;
    private final MembersListByAdultPdfGenerator membersListByAdultPdfGenerator;
    private final AllMembersAttendancePdfGenerator allMembersAttendancePdfGenerator;
    private final TournamentJudgesPdfGenerator tournamentJudgesPdfGenerator;
    private final MembersToPoliceReportPdfGenerator membersToPoliceReportPdfGenerator;
    private final MembersToErasedListPdfGenerator membersToErasedListPdfGenerator;
    private final GunRegistryPdfGenerator gunRegistryPdfGenerator;
    private final GunTransportCertificatePdfGenerator gunTransportCertificatePdfGenerator;
    private final ErasedMembersByDatePdfGenerator erasedMembersByDatePdfGenerator;
    private final JudgingReportByDatePdfGenerator judgingReportByDatePdfGenerator;
    private final EvidenceBookByDatePdfGenerator evidenceBookByDatePdfGenerator;
    private final WorkTimeReportPdfGenerator workTimeReportPdfGenerator;
    private final MembersWithLicensePdfGenerator membersWithLicensePdfGenerator;
    private final MemberLegitimationPdfGenerator memberLegitimationPdfGenerator;
    private final DziesiatkaMembershipPdfGenerator dziesiatkaMembershipPdfGenerator;

    public void store(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        FilesModel build = FilesModel.builder().name(fileName).type(file.getContentType()).data(file.getBytes()).size(file.getSize()).build();
        createFileEntity(build);
    }

    public void uploadCSVOthers(MultipartFile file) throws IOException {

        InputStream inputStream = file.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        List<String> list = br.lines().toList();
        list.forEach(e -> {
            String[] s = e.split(";");
            OtherPersonEntity otherPerson = otherPersonService.addPerson(s[2], OtherPerson.builder().address(new Address()).email("").phoneNumber("").memberPermissions(new MemberPermissions()).weaponPermissionNumber("").secondName(s[0].toUpperCase(Locale.ROOT)).firstName(s[1].substring(0, 1).toUpperCase(Locale.ROOT).concat(s[1].substring(1).toLowerCase(Locale.ROOT))).build());
            otherPersonRepository.save(otherPerson);
        });

    }

    public List<?> getAllMemberFiles(String uuid) {
        return filesRepository.findAllByBelongToMemberUUIDEquals(uuid);
    }

    public List<?> getAllFilesList(Pageable page) {
        page = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by("date").and(Sort.by("time")).descending());
        return filesRepository.findAllByDateIsNotNullAndTimeIsNotNull(page).stream().collect(Collectors.toList());
    }

    public FilesEntity getFile(String uuid) {
        return filesRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Plik nie istnieje: " + uuid));
    }

    public String store(MultipartFile file, MemberEntity member) throws IOException {
        String name = member.getLegitimationNumber() + member.getSecondName().toUpperCase() + member.getFirstName().toUpperCase();
        FilesModel build = FilesModel.builder().name(name).type(file.getContentType()).data(file.getBytes()).size(file.getSize()).belongToMemberUUID(member.getUuid()).build();
        FilesEntity fileEntity = createFileEntity(build);
        member.setImageUUID(fileEntity.getUuid());
        memberRepository.save(member);

        return fileEntity.getUuid();
    }

    public ResponseEntity<?> delete(String uuid) {

        if (filesRepository.existsById(uuid)) {

            MemberEntity memberEntity = memberRepository.findAll().stream().filter(f -> f.getImageUUID() != null).filter(f -> f.getImageUUID().equals(uuid)).findFirst().orElse(null);
            if (memberEntity != null) {
                memberEntity.setImageUUID(null);
                memberRepository.save(memberEntity);
            }

            filesRepository.deleteById(uuid);
            LOG.info("Usunięto plik");
            return ResponseEntity.ok("Usunięto plik");
        } else {
            return ResponseEntity.badRequest().body("Nie udało się usunąć");
        }

    }

    public ResponseEntity<?> removeImageFromGun(String gunUUID) {
        GunEntity gun = gunRepository.findById(gunUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono broni"));
        String imgUUID = gun.getImgUUID();
        if (imgUUID != null && filesRepository.existsById(imgUUID)) {
            filesRepository.deleteById(imgUUID);
        }
        gun.setImgUUID(null);
        gunRepository.save(gun);
        return ResponseEntity.ok("Usunięto zdjęcie");
    }

    public ResponseEntity<?> countPages() {
        return ResponseEntity.ok(filesRepository.countAllRecordsDividedBy50());
    }

    // podpis rejestr pobytu - klubowicz
    public String storeImageEvidenceBookMember(String imageString, String pesel) {
        String s = imageString.split(",")[1];
        MemberEntity memberEntity = memberRepository.findAllByErasedFalse().stream().filter(f -> f.getPesel().equals(pesel)).findFirst().orElseThrow(EntityNotFoundException::new);
        String fullName = memberEntity.getFullName();
        String fileName = fullName + " evidence.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis rejestr pobytu - pozostali
    public String storeImageEvidenceBook(ImageOtherPersonWrapper other, String imageString) {
        String s = imageString.split(",")[1];
        String fullName;
        if (other.getOther().getId() != null) {
            fullName = otherPersonRepository.findById(Integer.valueOf(other.getOther().getId())).orElseThrow(EntityNotFoundException::new).getFullName();
        } else {
            fullName = other.getOther().getFullName();
        }
        String fileName = fullName + " evidence.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - przyjmowanie amunicji na stan
    public String storeImageAddedAmmo(String imageString, String pinCode) {
        if (imageString == null || !imageString.contains(",")) {
            throw new IllegalArgumentException("Nieprawidłowy obraz");
        }
        String base64Data = imageString.split(",")[1];
        String hashedPin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(hashedPin).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("Nie znaleziono użytkownika");
        }
        String safeFileName = user.getFullName().replaceAll("\\s+", "_") + "_ammoAdded.png";
        byte[] imageBytes = Base64.getMimeDecoder().decode(base64Data);
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(safeFileName, imageBytes));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - wydawanie amunicji
    public String storeImageUpkeepAmmo(String imageString, String pinCode) {
        if (imageString == null || !imageString.contains(",")) {
            throw new IllegalArgumentException("Nieprawidłowy obraz");
        }
        String base64Data = imageString.split(",")[1];
        String hashedPin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(hashedPin).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("Nie znaleziono użytkownika");
        }
        String safeFileName = user.getFullName().replaceAll("\\s+", "_") + "_upkeepAmmo.png";
        byte[] imageBytes = Base64.getMimeDecoder().decode(base64Data);
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(safeFileName, imageBytes));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - wydawanie broni
    public String storeImageIssuanceGun(String imageString, String pinCode) {
        if (imageString == null || !imageString.contains(",")) {
            throw new IllegalArgumentException("Nieprawidłowy obraz");
        }
        String base64Data = imageString.split(",")[1];
        String hashedPin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(hashedPin).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("Nie znaleziono użytkownika");
        }
        String safeFileName = user.getFullName().replaceAll("\\s+", "_") + "_IssuanceGun.png";
        byte[] imageBytes = Base64.getMimeDecoder().decode(base64Data);
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(safeFileName, imageBytes));
        return fileEntity.getUuid();
    }

    // podpis osoby pobierającej broń
    public String storeImageTakerGun(String imageString, Integer memberLeg) {
        MemberEntity one = memberRepository.findByLegitimationNumber(memberLeg).orElseThrow(EntityNotFoundException::new);
        String s = imageString.split(",")[1];
        String fileName = one.getFullName() + " TakerGun.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis osoby oddającej broń
    public String storeImageReturnerGun(String imageString, Integer memberLeg) {
        MemberEntity one = memberRepository.findByLegitimationNumber(memberLeg).orElseThrow(EntityNotFoundException::new);
        String s = imageString.split(",")[1];
        String fileName = one.getFullName() + " ReturnerGun.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - usuwanie broni
    public String storeImageRemoveGun(String imageString, String takerName) {
        String s = imageString.split(",")[1];
        String fileName = takerName + " RemoveGun.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - wprowadzanie broni na stan
    public String storeImageAddGun(String imageString, String pinCode) {
        if (imageString == null || !imageString.contains(",")) {
            throw new IllegalArgumentException("Nieprawidłowy obraz");
        }
        String base64Data = imageString.split(",")[1];
        String hashedPin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(hashedPin).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("Nie znaleziono użytkownika");
        }
        String safeFileName = user.getFullName().replaceAll("\\s+", "_") + "_AddGun.png";
        byte[] imageBytes = Base64.getMimeDecoder().decode(base64Data);
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(safeFileName, imageBytes));
        return fileEntity.getUuid();
    }

    // potwierdzenie opłacenie składki
    public FilesEntity contributionConfirm(String memberUUID, String contributionUUID, Boolean a5rotate) throws DocumentException, IOException {
        PdfGenerationResults pdf = contributionConfirmPdfGenerator.generate(memberUUID, contributionUUID, a5rotate);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);

    }

    // karta członkowska dziesiątka
    public FilesEntity getMembershipDeclaration(String memberUUID) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ProfilesEnum activeProfile = ProfilesEnum.fromName(environment.getActiveProfiles()[0]);

        PdfGenerationResults pdf = switch (activeProfile) {
            case ProfilesEnum.TEST, ProfilesEnum.DZIESIATKA -> dziesiatkaMembershipPdfGenerator.generate(member);
            case ProfilesEnum.PANASZEW -> personalCardPdfGenerator.generate(member);
            case ProfilesEnum.GUARDIANS -> guardiansMembershipPdfGenerator.generate(member);
            case ProfilesEnum.MECHANIK -> personalCardPdfGenerator.generate(member);
        };
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    // karta członkowska guardians
    public FilesEntity getGuardiansMembershipDeclaration(String memberUUID) throws Exception {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = guardiansMembershipPdfGenerator.generate(member);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    // lista amunicyjna
    public FilesEntity createAmmunitionListDocument(String ammoEvidenceUUID) throws IOException, DocumentException {
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findById(ammoEvidenceUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = ammunitionListPdfGenerator.generate(ammoEvidenceEntity);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // wniosek o przedłużenie licencji zawodniczej
    public FilesEntity createApplicationForExtensionOfTheCompetitorsLicense(String memberUUID) throws IOException, DocumentException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = competitorLicenseExtensionPdfGenerator.generate(member);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    // zaświadczenie z Klubu RCS Panaszew
    public FilesEntity certificateOfClubMembershipPanaszew(String memberUUID, String reason) throws IOException, DocumentException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = certificateOfClubMembershipPanaszewPdfGenerator.generate(member, reason);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    // wniosek o pozwolenie na broń
    public FilesEntity ApplicationForFirearmsLicense(String memberUUID, String thirdName, String birthPlace, String fatherName, String motherName, String motherMaidenName, String issuingAuthority, LocalDate parseIDDate, LocalDate parselicenseDate, String city) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = applicationForFirearmsLicensePdfGenerator.generate(
                member, thirdName, birthPlace, fatherName, motherName,
                motherMaidenName, issuingAuthority, parseIDDate, parselicenseDate, city
        );
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    // zaświadczenie z Klubu Dziesiątka
    public FilesEntity CertificateOfClubMembership(String memberUUID, String reason, String city, boolean enlargement) throws IOException, DocumentException {
        PdfGenerationResults pdf = certificateOfClubMembershipDziesiatkaPdfGenerator.generate(memberUUID, reason, city, enlargement);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    // plik .csv klubowicza
    public FilesEntity getMemberCSVFile(String memberUUID) throws IOException {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        CsvGenerationResults csv = memberCsvGenerator.generate(memberEntity);
        return createFile(csv.fileName(), csv.data(), memberUUID);
    }

    // plik .csv maile do klubowiczów
    public FilesEntity getMemberEmailCSV() throws IOException {
        CsvGenerationResults csv = memberEmailCsvGenerator.generate();
        return createFile(csv.fileName(), csv.data(), null);
    }

    // metryczka
    public FilesEntity getStartsMetric(String memberUUID, String otherID, String tournamentUUID, List<String> competitions, String startNumber, Boolean a5rotate) throws IOException, DocumentException {
        PdfGenerationResults pdf = startsMetricPdfGenerator.generate(memberUUID, otherID, tournamentUUID, competitions, startNumber, a5rotate);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    // lista klubowiczów - grupa wiekowa
    public FilesEntity generateMembersListByAdult() throws IOException, DocumentException {
        PdfGenerationResults pdf = membersListByAdultPdfGenerator.generate();
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // lista Klubowiczów - lista obecności
    public FilesEntity generateAllMembersList() throws IOException, DocumentException {
        PdfGenerationResults pdf = allMembersAttendancePdfGenerator.generate();
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // lista sędziów na zawodach
    public FilesEntity getJudge(String tournamentUUID) throws IOException, DocumentException {
        PdfGenerationResults pdf = tournamentJudgesPdfGenerator.generate(tournamentUUID);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // lista osób do zgłoszenia na policję
    public FilesEntity generateListOfMembersToReportToPolice() throws IOException, DocumentException {
        PdfGenerationResults pdf = membersToPoliceReportPdfGenerator.generate();
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // lista osób do skreślenia
    public FilesEntity generateAllMembersToErasedList() throws IOException, DocumentException {
        PdfGenerationResults pdf = membersToErasedListPdfGenerator.generate();
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // lista broni - według podanej listy
    public FilesEntity getGunRegistry(List<String> guns) throws IOException, DocumentException {
        PdfGenerationResults pdf = gunRegistryPdfGenerator.generate(guns);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // list przewozowy
    public FilesEntity getGunTransportCertificate(List<String> guns, LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {
        PdfGenerationResults pdf = gunTransportCertificatePdfGenerator.generate(guns, firstDate, secondDate);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // lista osób skreślonych - z zakresu dat
    public FilesEntity getAllErasedMembers(LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {
        PdfGenerationResults pdf = erasedMembersByDatePdfGenerator.generate(firstDate, secondDate);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // raport sędziowania - z zakresu dat
    public FilesEntity getJudgingReportInChosenTime(LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {
        PdfGenerationResults pdf = judgingReportByDatePdfGenerator.generate(firstDate, secondDate);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // rejestr pobytu na strzelnicy - z zakresu dat
    public FilesEntity getEvidenceBookInChosenTime(LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {

        Map<String, byte[]> images = filesRepository.findAll().stream()
                .collect(Collectors.toMap(
                        FilesEntity::getUuid,
                        FilesEntity::getData
                ));
        PdfGenerationResults pdf = evidenceBookByDatePdfGenerator.generate(firstDate, secondDate,images);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // raport czasu pracy
    public FilesEntity getWorkTimeReport(int year, String month, boolean detailed) throws IOException, DocumentException {
        PdfGenerationResults pdf = workTimeReportPdfGenerator.generate(year, month, detailed);
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // lista osób z licencjami
    public FilesEntity generateMembersListWithLicense() throws IOException, DocumentException {
        PdfGenerationResults pdf = membersWithLicensePdfGenerator.generate();
        return createFile(pdf.fileName(), pdf.data(), null);
    }

    // Deklaracja LOK
    public FilesEntity getMembershipDeclarationLOK(String memberUUID) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = lokMembershipPdfGenerator.generate(member);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }
    // Legitymacja klubowicza
    public FilesEntity getMemberLegitimationPdfGenerator(String memberUUID) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = memberLegitimationPdfGenerator.generate(member);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }

    private FilesModel getFilesModelPNG(String fileName, byte[] data) {
        return FilesModel.builder().name(fileName).data(data).type(String.valueOf(MediaType.IMAGE_PNG)).size(data.length).build();
    }

    public FilesEntity createFile(String fileName, byte[] data, String memberUUID) {
        FilesModel model = FilesModel.builder()
                .name(fileName)
                .belongToMemberUUID(memberUUID)
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

