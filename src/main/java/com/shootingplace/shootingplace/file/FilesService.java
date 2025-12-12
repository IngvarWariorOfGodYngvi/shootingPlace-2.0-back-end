package com.shootingplace.shootingplace.file;

import com.google.common.hash.Hashing;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.armory.GunStoreEntity;
import com.shootingplace.shootingplace.armory.GunStoreRepository;
import com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordEntity;
import com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordRepository;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.csv.generator.MemberCsvGenerator;
import com.shootingplace.shootingplace.file.csv.generator.MemberEmailCsvGenerator;
import com.shootingplace.shootingplace.file.csv.model.CsvGenerationResults;
import com.shootingplace.shootingplace.file.pdf.generator.*;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.history.JudgingHistoryEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import com.shootingplace.shootingplace.otherPerson.OtherPerson;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonService;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.utils.PageStamper;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceEntity;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import com.shootingplace.shootingplace.wrappers.ImageOtherPersonWrapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
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

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilesService {

    private final MemberRepository memberRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final FilesRepository filesRepository;
    private final TournamentRepository tournamentRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final GunRepository gunRepository;
    private final ContributionRepository contributionRepository;
    private final GunStoreRepository gunStoreRepository;
    private final WorkingTimeEvidenceRepository workRepo;
    private final WorkingTimeEvidenceService workServ;
    private final Environment environment;
    private final RegistrationRecordRepository registrationRepo;
    private final OtherPersonService otherPersonService;
    private final UserRepository userRepository;
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

    private FilesEntity createFileEntity(FilesModel filesModel) {
        filesModel.setDate(LocalDate.now());
        filesModel.setTime(LocalTime.now());
        FilesEntity filesEntity = Mapping.map(filesModel);
        LOG.info(filesModel.getName().trim() + " Encja została zapisana");
        return filesRepository.save(filesEntity);

    }

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

    // podpis magazyniera - usuwanie bromi
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
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ContributionEntity contribution = contributionRepository.findById(contributionUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = contributionConfirmPdfGenerator.generate(member, contribution, a5rotate);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);

    }

    // karta członkowska dzesiątka
    public FilesEntity personalCardFile(String memberUUID) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = personalCardPdfGenerator.generate(member);
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
    public FilesEntity generateMembersListByAdult(boolean adult) throws IOException, DocumentException {
        String fileName = "Lista_klubowiczów_na_dzień " + LocalDate.now().format(dateFormat()) + ".pdf";
        Document document = new Document(PageSize.A4.rotate());
        setAttToDoc(fileName, document, true, true);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        List<MemberEntity> all = memberRepository.findAll().stream().filter(f -> !f.isErased()).filter(f -> f.isAdult() == adult).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).toList();

        Paragraph title = new Paragraph("Lista klubowiczów na dzień " + LocalDate.now().format(dateFormat()), font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));
        document.add(title);
        document.add(newLine);

        float[] pointColumnWidths = {4F, 58F, 10F, 12F, 12F, 12F};

        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        titleTable.setWidthPercentage(100);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell LegNumber = new PdfPCell(new Paragraph("legitymacja", font(12, 0)));
        PdfPCell inDate = new PdfPCell(new Paragraph("Data zapisu", font(12, 0)));
        PdfPCell contributionDate = new PdfPCell(new Paragraph("Data opłacenia składki", font(12, 0)));
        PdfPCell contributionValidThru = new PdfPCell(new Paragraph("Składka ważna do", font(12, 0)));

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(LegNumber);
        titleTable.addCell(inDate);
        titleTable.addCell(contributionDate);
        titleTable.addCell(contributionValidThru);

        document.add(titleTable);
        document.add(newLine);

        for (int i = 0; i < all.size(); i++) {
            MemberEntity memberEntity = all.get(i);
            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            memberTable.setWidthPercentage(100);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntity.getSecondName().concat(" " + memberEntity.getFirstName()), font(12, 0)));
            PdfPCell legNumberCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell inDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getJoinDate()), font(12, 0)));
            PdfPCell contributionDateCell;
            PdfPCell contributionValidThruCell;
            if (memberEntity.getHistory().getContributionList().size() > 0) {
                contributionDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getPaymentDay()), font(12, 0)));
                contributionValidThruCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getValidThru()), font(12, 0)));
            } else {
                contributionDateCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 1)));
                contributionValidThruCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 1)));
            }
            if (!memberEntity.isActive()) {
                contributionDateCell.setBackgroundColor(Color.RED);
                contributionValidThruCell.setBackgroundColor(Color.RED);
            }
            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legNumberCell);
            memberTable.addCell(inDateCell);
            memberTable.addCell(contributionDateCell);
            memberTable.addCell(contributionValidThruCell);
            document.add(memberTable);
        }
        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // lista Klubowiczów - lista obecności
    public FilesEntity generateAllMembersList() throws IOException, DocumentException {
        String fileName = "Lista_obecności_klubowiczów " + LocalDate.now().format(dateFormat()) + ".pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, true);
        document.open();
        document.setMarginMirroringTopBottom(true);
        document.addTitle(fileName);
        document.addCreationDate();
        List<MemberEntity> all = memberRepository.findAll().stream().filter(f -> !f.isErased()).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).toList();
        Paragraph title = new Paragraph("Lista obecności klubowiczów na dzień " + LocalDate.now().format(dateFormat()), font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));
        document.add(title);
        document.add(newLine);
        float[] pointColumnWidths = {3F, 25F, 15F, 20F};
        PdfPTable titleTable = new PdfPTable(pointColumnWidths);
        titleTable.setWidthPercentage(100);
        Paragraph lp1 = new Paragraph("lp", font(12, 0));
        PdfPCell lp = new PdfPCell(lp1);
        Paragraph name1 = new Paragraph("Nazwisko Imię", font(12, 0));
        PdfPCell name = new PdfPCell(name1);
        Paragraph legNumber1 = new Paragraph("Legitymacja", font(12, 0));
        PdfPCell legNumber = new PdfPCell(legNumber1);
        Paragraph signature1 = new Paragraph("Podpis", font(12, 0));
        PdfPCell signature = new PdfPCell(signature1);
        lp.setHorizontalAlignment(1);
        name.setHorizontalAlignment(1);
        legNumber.setHorizontalAlignment(1);
        signature.setHorizontalAlignment(1);
        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(legNumber);
        titleTable.addCell(signature);

        document.add(titleTable);
        document.add(newLine);

        for (int i = 0; i < all.size(); i++) {
            MemberEntity memberEntity = all.get(i);
            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            memberTable.setWidthPercentage(100);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntity.getSecondName().concat(" " + memberEntity.getFirstName()), font(12, 0)));
            PdfPCell legNumberCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell signatureCell = new PdfPCell(new Paragraph(" ", font(12, 0)));

            lpCell.setHorizontalAlignment(1);
            legNumberCell.setHorizontalAlignment(1);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legNumberCell);
            memberTable.addCell(signatureCell);

            document.add(memberTable);
        }
        document.close();

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // lista sędziów na zawodach
    public FilesEntity getJudge(String tournamentUUID) throws IOException, DocumentException {

        TournamentEntity tournamentEntity = tournamentRepository.getOne(tournamentUUID);

        String fileName = "Lista_sędziów_na_zawodach_" + tournamentEntity.getName() + ".pdf";

        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        Paragraph title = new Paragraph(tournamentEntity.getName().toUpperCase(), font(13, 1));
        Paragraph date = new Paragraph((environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "") + ", " + monthFormat(tournamentEntity.getDate()), font(10, 2));

        Paragraph listTitle = new Paragraph("WYKAZ SĘDZIÓW", font(13, 0));
        Paragraph newLine = new Paragraph("\n", font(13, 0));

        Paragraph subTitle = new Paragraph("\nSędzia Główny", font(13, 0));
        Paragraph subTitle1 = new Paragraph("\nPrzewodniczący Komisji RTS", font(13, 0));

        String mainArbiter;
        String mainArbiterClass;

        if (tournamentEntity.getMainArbiter() != null) {
            mainArbiter = tournamentEntity.getMainArbiter().getFirstName() + " " + tournamentEntity.getMainArbiter().getSecondName();
            mainArbiterClass = tournamentEntity.getMainArbiter().getMemberPermissions().getArbiterClass();
            mainArbiterClass = getArbiterClass(mainArbiterClass);
        } else {
            if (tournamentEntity.getOtherMainArbiter() != null) {
                mainArbiter = tournamentEntity.getOtherMainArbiter().getFirstName() + " " + tournamentEntity.getOtherMainArbiter().getSecondName();
                mainArbiterClass = tournamentEntity.getOtherMainArbiter().getPermissionsEntity().getArbiterClass();
                mainArbiterClass = getArbiterClass(mainArbiterClass);
            } else {
                mainArbiter = "Nie Wskazano";
                mainArbiterClass = "";
            }
        }

        Paragraph mainArbiterOnTournament = new Paragraph(mainArbiter + " " + mainArbiterClass, font(12, 0));

        String commissionRTSArbiter;
        String commissionRTSArbiterClass;

        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            commissionRTSArbiter = tournamentEntity.getCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getCommissionRTSArbiter().getSecondName();
            commissionRTSArbiterClass = tournamentEntity.getCommissionRTSArbiter().getMemberPermissions().getArbiterClass();
            commissionRTSArbiterClass = getArbiterClass(commissionRTSArbiterClass);
        } else {
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                commissionRTSArbiter = tournamentEntity.getOtherCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getOtherCommissionRTSArbiter().getSecondName();
                commissionRTSArbiterClass = tournamentEntity.getOtherCommissionRTSArbiter().getPermissionsEntity().getArbiterClass();
                commissionRTSArbiterClass = getArbiterClass(commissionRTSArbiterClass);
            } else {
                commissionRTSArbiter = "Nie Wskazano";
                commissionRTSArbiterClass = "";
            }
        }

        Paragraph commissionRTSArbiterOnTournament = new Paragraph(commissionRTSArbiter + " " + commissionRTSArbiterClass, font(12, 0));

        Paragraph others = new Paragraph("\nSędziowie Stanowiskowi\n", font(12, 0));
        Paragraph others1 = new Paragraph("\nSędziowie Biura Obliczeń\n", font(12, 0));

        document.add(title);
        document.add(date);
        document.add(newLine);
        document.add(listTitle);
        document.add(subTitle);
        document.add(mainArbiterOnTournament);
        document.add(subTitle1);
        document.add(commissionRTSArbiterOnTournament);

        if (tournamentEntity.getArbitersList().size() > 0 || tournamentEntity.getOtherArbitersList().size() > 0) {
            document.add(others);
        }

        List<MemberEntity> arbitersList = tournamentEntity.getArbitersList();
        if (arbitersList.size() > 0) {
            for (MemberEntity entity : arbitersList) {
                String arbiterClass = entity.getMemberPermissions().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherArbiter = new Paragraph(entity.getFirstName().concat(" " + entity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherArbiter);
            }
        }

        List<OtherPersonEntity> otherArbitersList = tournamentEntity.getOtherArbitersList();
        if (otherArbitersList.size() > 0) {

            for (OtherPersonEntity personEntity : otherArbitersList) {
                String arbiterClass = personEntity.getPermissionsEntity().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherArbiters = new Paragraph(personEntity.getFirstName().concat(" " + personEntity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherArbiters);
            }

        }

        if (tournamentEntity.getArbitersRTSList().size() > 0 || tournamentEntity.getOtherArbitersRTSList().size() > 0) {
            document.add(others1);
        }

        List<MemberEntity> arbitersRTSList = tournamentEntity.getArbitersRTSList();
        if (arbitersRTSList.size() > 0) {
            for (MemberEntity entity : arbitersRTSList) {
                String arbiterClass = entity.getMemberPermissions().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherRTSArbiter = new Paragraph(entity.getFirstName().concat(" " + entity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherRTSArbiter);
            }
        }

        List<OtherPersonEntity> otherArbitersRTSList = tournamentEntity.getOtherArbitersRTSList();
        if (otherArbitersRTSList.size() > 0) {
            for (OtherPersonEntity personEntity : otherArbitersRTSList) {
                String arbiterClass = personEntity.getPermissionsEntity().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherPersonRTSArbiter = new Paragraph(personEntity.getFirstName().concat(" " + personEntity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherPersonRTSArbiter);
            }
        }

        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;

    }

    // lista osób do zgłoszenia na policję
    public FilesEntity generateListOfMembersToReportToPolice() throws IOException, DocumentException {

        String fileName = "Lista_osób_do_zgłoszenia_na_Policję " + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        setAttToDoc(fileName, document, true, true);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        String minute;
        if (LocalTime.now().getMinute() < 10) {
            minute = "0" + LocalTime.now().getMinute();
        } else {
            minute = String.valueOf(LocalTime.now().getMinute());
        }
        String now = LocalTime.now().getHour() + ":" + minute;
        Paragraph title = new Paragraph("Lista osób do zgłoszenia na policję " + LocalDate.now().format(dateFormat()) + " " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);
        LocalDate notValidLicense = LocalDate.now().minusMonths(6);
        List<MemberEntity> memberEntityList = memberRepository.findAll().stream().filter(f -> !f.isErased()).filter(f -> f.getLicense().getNumber() != null).filter(f -> !f.getLicense().isValid()).filter(f -> f.getClub().getId() == 1).filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense)).sorted(Comparator.comparing(MemberEntity::getSecondName, pl())).toList();
        float[] pointColumnWidths = {7F, 44F, 17F, 17F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell pesel = new PdfPCell(new Paragraph("PESEL", font(12, 0)));
        PdfPCell licenceNumber = new PdfPCell(new Paragraph("numer licencji", font(12, 0)));

        titleTable.setWidthPercentage(100);

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(pesel);
        titleTable.addCell(licenceNumber);

        document.add(titleTable);

        document.add(newLine);

        for (int i = 0; i < memberEntityList.size(); i++) {

            MemberEntity memberEntity = memberEntityList.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell peselCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getPesel()), font(12, 0)));
            PdfPCell licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));

            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(peselCell);
            memberTable.addCell(licenceNumberCell);

            document.add(memberTable);

        }


        document.close();

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // lista osób do skreślenia
    public FilesEntity generateAllMembersToErasedList() throws IOException, DocumentException {

        String fileName = "Lista osób do skreślenia na dzień " + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        setAttToDoc(fileName, document, true, true);

        Paragraph title = new Paragraph("Lista osób do skreślenia na dzień " + LocalDate.now().format(dateFormat()), font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);
        LocalDate notValidDate = LocalDate.now().minusMonths(6);

        List<MemberEntity> members = memberRepository.findAllByErasedFalseAndActiveFalse().stream().filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidDate)).sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))).toList();
        float[] pointColumnWidths = {4F, 42F, 14F, 14F, 14F, 14F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell legitimation = new PdfPCell(new Paragraph("legitymacja", font(12, 0)));
        PdfPCell licenceNumber = new PdfPCell(new Paragraph("numer licencji", font(12, 0)));
        PdfPCell licenceDate = new PdfPCell(new Paragraph("licencja ważna do", font(12, 0)));
        PdfPCell contributionDate = new PdfPCell(new Paragraph("Składka ważna do", font(12, 0)));

        titleTable.setWidthPercentage(100);

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(legitimation);
        titleTable.addCell(licenceNumber);
        titleTable.addCell(licenceDate);
        titleTable.addCell(contributionDate);

        document.add(titleTable);

        document.add(newLine);

        for (int i = 0; i < members.size(); i++) {

            MemberEntity memberEntity = members.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell legitimationCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell licenceNumberCell;
            if (memberEntity.getLicense().getNumber() != null) {
                licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            } else {
                licenceNumberCell = new PdfPCell(new Paragraph("", font(12, 0)));
            }
            PdfPCell licenceDateCell;
            if (memberEntity.getLicense().getNumber() != null) {
                licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru()), font(12, 0)));
            } else {
                licenceDateCell = new PdfPCell(new Paragraph("", font(12, 0)));
            }
            PdfPCell contributionDateCell;
            if (memberEntity.getHistory().getContributionList().size() > 0) {
                contributionDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getValidThru()), font(12, 0)));
            } else {
                contributionDateCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 0)));
            }
            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legitimationCell);
            memberTable.addCell(licenceNumberCell);
            memberTable.addCell(licenceDateCell);
            memberTable.addCell(contributionDateCell);

            document.add(memberTable);

        }


        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // lista broni - według podanej listy
    public FilesEntity getGunRegistry(List<String> guns) throws IOException, DocumentException {
        String fileName = "Lista_broni_w_magazynie_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph title = new Paragraph("Lista Broni w Magazynie".toUpperCase(), font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);

        float[] pointColumnWidths = {4F, 16F, 10F, 10F, 10F, 10F, 10F};
        int tableContentSize = 10;
        Paragraph lp = new Paragraph("Lp", font(tableContentSize, 0));
        Paragraph modelName = new Paragraph("Marka i Model", font(tableContentSize, 0));
        Paragraph caliberAndProductionYear = new Paragraph("Kaliber i rok produkcji", font(tableContentSize, 0));
        Paragraph serialNumber = new Paragraph("Numer i seria", font(tableContentSize, 0));
        Paragraph recordInEvidenceBook = new Paragraph("Poz. z książki ewidencji", font(tableContentSize, 0));
        Paragraph numberOfMagazines = new Paragraph("Magazynki", font(tableContentSize, 0));
        Paragraph gunCertificateSerialNumber = new Paragraph("Numer świadectwa", font(tableContentSize, 0));


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);
        titleTable.setWidthPercentage(100);

        PdfPCell lpCell = new PdfPCell(lp);
        PdfPCell modelNameCell = new PdfPCell(modelName);
        PdfPCell caliberAndProductionYearCell = new PdfPCell(caliberAndProductionYear);
        PdfPCell serialNumberCell = new PdfPCell(serialNumber);
        PdfPCell recordInEvidenceBookCell = new PdfPCell(recordInEvidenceBook);
        PdfPCell numberOfMagazinesCell = new PdfPCell(numberOfMagazines);
        PdfPCell gunCertificateSerialNumberCell = new PdfPCell(gunCertificateSerialNumber);

        lpCell.setHorizontalAlignment(1);
        lpCell.setVerticalAlignment(1);
        modelNameCell.setHorizontalAlignment(1);
        modelNameCell.setVerticalAlignment(1);
        caliberAndProductionYearCell.setHorizontalAlignment(1);
        caliberAndProductionYearCell.setVerticalAlignment(1);
        serialNumberCell.setHorizontalAlignment(1);
        serialNumberCell.setVerticalAlignment(1);
        recordInEvidenceBookCell.setHorizontalAlignment(1);
        recordInEvidenceBookCell.setVerticalAlignment(1);
        numberOfMagazinesCell.setHorizontalAlignment(1);
        numberOfMagazinesCell.setVerticalAlignment(1);
        gunCertificateSerialNumberCell.setHorizontalAlignment(1);
        gunCertificateSerialNumberCell.setVerticalAlignment(1);


        titleTable.addCell(lpCell);
        titleTable.addCell(modelNameCell);
        titleTable.addCell(caliberAndProductionYearCell);
        titleTable.addCell(serialNumberCell);
        titleTable.addCell(recordInEvidenceBookCell);
        titleTable.addCell(numberOfMagazinesCell);
        titleTable.addCell(gunCertificateSerialNumberCell);


        List<String> list = new ArrayList<>();

        for (int i = 0; i < guns.size(); i++) {
            int finalI = i;
            GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getUuid().equals(guns.get(finalI))).findFirst().orElseThrow(EntityNotFoundException::new);
            if (!gunStoreEntity.getGunEntityList().isEmpty()) {
                list.add(gunStoreEntity.getTypeName());
            }
            list.sort(String::compareTo);
        }

        for (int i = 0; i < list.size(); i++) {
            Paragraph gunTypeName = new Paragraph(list.get(i), font(12, 1));
            gunTypeName.setAlignment(1);
            document.add(gunTypeName);
            document.add(newLine);
            document.add(titleTable);

            int finalI = i;
            List<GunEntity> collect = gunRepository.findAll().stream().filter(f -> f.getGunType().equals(list.get(finalI))).filter(GunEntity::isInStock).sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName)).toList();
            if (collect.size() > 0) {

                for (int j = 0; j < collect.size(); j++) {
                    int contentSize = 8;
                    GunEntity gun = collect.get(j);

                    PdfPTable gunTable = new PdfPTable(pointColumnWidths);
                    gunTable.setWidthPercentage(100);

                    Paragraph lpGun = new Paragraph(String.valueOf(j + 1), font(contentSize, 0));
                    Paragraph modelNameGun = new Paragraph(gun.getModelName(), font(contentSize, 0));
                    Paragraph caliberAndProductionYearGun;
                    if (gun.getProductionYear() != null && !gun.getProductionYear().isEmpty() && !gun.getProductionYear().equals("null")) {
                        caliberAndProductionYearGun = new Paragraph(gun.getCaliber() + "\nrok " + gun.getProductionYear(), font(contentSize, 0));

                    } else {
                        caliberAndProductionYearGun = new Paragraph(gun.getCaliber(), font(contentSize, 0));
                    }
                    Paragraph serialNumberGun = new Paragraph(gun.getSerialNumber(), font(contentSize, 0));
                    Paragraph recordInEvidenceBookGun = new Paragraph(gun.getRecordInEvidenceBook(), font(contentSize, 0));
                    Paragraph numberOfMagazinesGun = new Paragraph(gun.getNumberOfMagazines(), font(contentSize, 0));
                    Paragraph gunCertificateSerialNumberGun = new Paragraph(gun.getGunCertificateSerialNumber(), font(contentSize, 0));

                    PdfPCell lpGunCell = new PdfPCell(lpGun);
                    PdfPCell modelNameGunCell = new PdfPCell(modelNameGun);
                    PdfPCell caliberAndProductionYearGunCell = new PdfPCell(caliberAndProductionYearGun);
                    PdfPCell serialNumberGunCell = new PdfPCell(serialNumberGun);
                    PdfPCell recordInEvidenceBookGunCell = new PdfPCell(recordInEvidenceBookGun);
                    PdfPCell numberOfMagazinesGunCell = new PdfPCell(numberOfMagazinesGun);
                    PdfPCell gunCertificateSerialNumberGunCell = new PdfPCell(gunCertificateSerialNumberGun);

                    lpGunCell.setHorizontalAlignment(1);
                    lpGunCell.setVerticalAlignment(1);
                    modelNameGunCell.setHorizontalAlignment(1);
                    modelNameGunCell.setVerticalAlignment(1);
                    caliberAndProductionYearGunCell.setHorizontalAlignment(1);
                    caliberAndProductionYearGunCell.setVerticalAlignment(1);
                    serialNumberGunCell.setHorizontalAlignment(1);
                    serialNumberGunCell.setVerticalAlignment(1);
                    recordInEvidenceBookGunCell.setHorizontalAlignment(1);
                    recordInEvidenceBookGunCell.setVerticalAlignment(1);
                    numberOfMagazinesGunCell.setHorizontalAlignment(1);
                    numberOfMagazinesGunCell.setVerticalAlignment(1);
                    gunCertificateSerialNumberGunCell.setHorizontalAlignment(1);
                    gunCertificateSerialNumberGunCell.setVerticalAlignment(1);

                    gunTable.addCell(lpGunCell);
                    gunTable.addCell(modelNameGunCell);
                    gunTable.addCell(caliberAndProductionYearGunCell);
                    gunTable.addCell(serialNumberGunCell);
                    gunTable.addCell(recordInEvidenceBookGunCell);
                    gunTable.addCell(numberOfMagazinesGunCell);
                    gunTable.addCell(gunCertificateSerialNumberGunCell);

                    document.add(gunTable);
                }
                document.add(newLine);

            }

        }
        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;

    }

    // list przewozowy
    public FilesEntity getGunTransportCertificate(List<String> guns, LocalDate date, LocalDate date1) throws IOException, DocumentException {
        String fileName = "Lista_broni_do_przewozu_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";
        Document document = new Document(PageSize.A4.rotate());
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        String minute;
        if (LocalTime.now().getMinute() < 10) {
            minute = "0" + LocalTime.now().getMinute();
        } else {
            minute = String.valueOf(LocalTime.now().getMinute());
        }
        String now = LocalTime.now().getHour() + ":" + minute;
        Paragraph title = new Paragraph("Lista jednostek broni do przewozu od dnia " + date + " do dnia " + date1, font(14, 1));
        Paragraph subtitle = new Paragraph("Wystawiono dnia " + LocalDate.now().format(dateFormat()) + " o godzinie " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(subtitle);
        document.add(newLine);

        float[] pointColumnWidths = {4F, 16F, 12F, 12F, 12F, 12F, 12F};

        Paragraph lp = new Paragraph("Lp", font(12, 0));
        Paragraph modelName = new Paragraph("Marka i Model", font(12, 0));
        Paragraph caliberAndProductionYear = new Paragraph("Kaliber i rok produkcji", font(12, 0));
        Paragraph serialNumber = new Paragraph("Numer i seria", font(12, 0));
        Paragraph recordInEvidenceBook = new Paragraph("Poz. z książki ewidencji", font(12, 0));
        Paragraph numberOfMagazines = new Paragraph("Magazynki", font(12, 0));
        Paragraph gunCertificateSerialNumber = new Paragraph("Numer świadectwa", font(12, 0));


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);
        titleTable.setWidthPercentage(100);

        PdfPCell lpCell = new PdfPCell(lp);
        PdfPCell modelNameCell = new PdfPCell(modelName);
        PdfPCell caliberAndProductionYearCell = new PdfPCell(caliberAndProductionYear);
        PdfPCell serialNumberCell = new PdfPCell(serialNumber);
        PdfPCell recordInEvidenceBookCell = new PdfPCell(recordInEvidenceBook);
        PdfPCell numberOfMagazinesCell = new PdfPCell(numberOfMagazines);
        PdfPCell gunCertificateSerialNumberCell = new PdfPCell(gunCertificateSerialNumber);

        lpCell.setHorizontalAlignment(1);
        lpCell.setVerticalAlignment(1);
        modelNameCell.setHorizontalAlignment(1);
        modelNameCell.setVerticalAlignment(1);
        caliberAndProductionYearCell.setHorizontalAlignment(1);
        caliberAndProductionYearCell.setVerticalAlignment(1);
        serialNumberCell.setHorizontalAlignment(1);
        serialNumberCell.setVerticalAlignment(1);
        recordInEvidenceBookCell.setHorizontalAlignment(1);
        recordInEvidenceBookCell.setVerticalAlignment(1);
        numberOfMagazinesCell.setHorizontalAlignment(1);
        numberOfMagazinesCell.setVerticalAlignment(1);
        gunCertificateSerialNumberCell.setHorizontalAlignment(1);
        gunCertificateSerialNumberCell.setVerticalAlignment(1);


        titleTable.addCell(lpCell);
        titleTable.addCell(modelNameCell);
        titleTable.addCell(caliberAndProductionYearCell);
        titleTable.addCell(serialNumberCell);
        titleTable.addCell(recordInEvidenceBookCell);
        titleTable.addCell(numberOfMagazinesCell);
        titleTable.addCell(gunCertificateSerialNumberCell);

        document.add(titleTable);

        List<GunEntity> collect1 = new ArrayList<>();
        List<GunEntity> finalCollect = collect1;
        guns.forEach(e -> finalCollect.add(gunRepository.getReferenceById(e)));
        collect1 = collect1.stream().sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName)).collect(Collectors.toList());

        for (int j = 0; j < collect1.size(); j++) {

            GunEntity gun = collect1.get(j);

            PdfPTable gunTable = new PdfPTable(pointColumnWidths);
            gunTable.setWidthPercentage(100);

            Paragraph lpGun = new Paragraph(String.valueOf(j + 1), font(12, 0));
            Paragraph modelNameGun = new Paragraph(gun.getModelName(), font(12, 0));
            Paragraph caliberAndProductionYearGun;
            if (gun.getProductionYear() != null && !gun.getProductionYear().isEmpty() && !gun.getProductionYear().equals("null")) {
                caliberAndProductionYearGun = new Paragraph(gun.getCaliber() + "\nrok " + gun.getProductionYear(), font(12, 0));

            } else {
                caliberAndProductionYearGun = new Paragraph(gun.getCaliber(), font(12, 0));
            }
            Paragraph serialNumberGun = new Paragraph(gun.getSerialNumber(), font(12, 0));
            Paragraph recordInEvidenceBookGun = new Paragraph(gun.getRecordInEvidenceBook(), font(12, 0));
            Paragraph numberOfMagazinesGun = new Paragraph(gun.getNumberOfMagazines(), font(12, 0));
            Paragraph gunCertificateSerialNumberGun = new Paragraph(gun.getGunCertificateSerialNumber(), font(12, 0));

            PdfPCell lpGunCell = new PdfPCell(lpGun);
            PdfPCell modelNameGunCell = new PdfPCell(modelNameGun);
            PdfPCell caliberAndProductionYearGunCell = new PdfPCell(caliberAndProductionYearGun);
            PdfPCell serialNumberGunCell = new PdfPCell(serialNumberGun);
            PdfPCell recordInEvidenceBookGunCell = new PdfPCell(recordInEvidenceBookGun);
            PdfPCell numberOfMagazinesGunCell = new PdfPCell(numberOfMagazinesGun);
            PdfPCell gunCertificateSerialNumberGunCell = new PdfPCell(gunCertificateSerialNumberGun);

            lpGunCell.setHorizontalAlignment(1);
            lpGunCell.setVerticalAlignment(1);
            modelNameGunCell.setHorizontalAlignment(1);
            modelNameGunCell.setVerticalAlignment(1);
            caliberAndProductionYearGunCell.setHorizontalAlignment(1);
            caliberAndProductionYearGunCell.setVerticalAlignment(1);
            serialNumberGunCell.setHorizontalAlignment(1);
            serialNumberGunCell.setVerticalAlignment(1);
            recordInEvidenceBookGunCell.setHorizontalAlignment(1);
            recordInEvidenceBookGunCell.setVerticalAlignment(1);
            numberOfMagazinesGunCell.setHorizontalAlignment(1);
            numberOfMagazinesGunCell.setVerticalAlignment(1);
            gunCertificateSerialNumberGunCell.setHorizontalAlignment(1);
            gunCertificateSerialNumberGunCell.setVerticalAlignment(1);

            gunTable.addCell(lpGunCell);
            gunTable.addCell(modelNameGunCell);
            gunTable.addCell(caliberAndProductionYearGunCell);
            gunTable.addCell(serialNumberGunCell);
            gunTable.addCell(recordInEvidenceBookGunCell);
            gunTable.addCell(numberOfMagazinesGunCell);
            gunTable.addCell(gunCertificateSerialNumberGunCell);

            document.add(gunTable);


        }

        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // lista osób skreślonych - z zakresu dat
    public FilesEntity getAllErasedMembers(LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {
        String fileName = "Lista osób skreślonych od " + firstDate + " do" + secondDate + ".pdf";
        Document document = new Document(PageSize.A4.rotate());
        setAttToDoc(fileName, document, true, true);
        Paragraph title = new Paragraph("Lista osób skreślonych od " + firstDate.format(dateFormat()) + " do " + secondDate.format(dateFormat()), font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);
        List<MemberEntity> memberEntityList = memberRepository.findAllByErasedTrue().stream().filter(f -> f.getErasedEntity() != null).filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)) && f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))).toList();
        float[] pointColumnWidths = {4F, 28F, 10F, 14F, 14F, 36F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);
        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell legitimation = new PdfPCell(new Paragraph("legitymacja", font(12, 0)));
        PdfPCell PESEL = new PdfPCell(new Paragraph("PESEL", font(12, 0)));
        PdfPCell erasedReason = new PdfPCell(new Paragraph("Przyczyna skreślenia", font(12, 0)));
        PdfPCell erasedAdditionalDescription = new PdfPCell(new Paragraph("Informacje dodatkowe", font(12, 0)));

        titleTable.setWidthPercentage(100);
        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(legitimation);
        titleTable.addCell(PESEL);
        titleTable.addCell(erasedReason);
        titleTable.addCell(erasedAdditionalDescription);

        document.add(titleTable);

        document.add(newLine);

        for (int i = 0; i < memberEntityList.size(); i++) {

            MemberEntity memberEntity = memberEntityList.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell legitimationCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell PESELCell = new PdfPCell(new Paragraph(memberEntity.getPesel(), font(12, 0)));
            PdfPCell erasedReasonCell = null;
            if (memberEntity.getErasedEntity() != null) {
                erasedReasonCell = new PdfPCell(new Paragraph(memberEntity.getErasedEntity().getErasedType() + " " + memberEntity.getErasedEntity().getDate().format(dateFormat()), font(12, 0)));
            }
            PdfPCell erasedAdditionalDescriptionCell;
            if (memberEntity.getErasedEntity() != null && memberEntity.getErasedEntity().getAdditionalDescription() != null) {
                erasedAdditionalDescriptionCell = new PdfPCell(new Paragraph(memberEntity.getErasedEntity().getAdditionalDescription(), font(12, 0)));
            } else {
                erasedAdditionalDescriptionCell = new PdfPCell(new Paragraph(""));
            }
            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legitimationCell);
            memberTable.addCell(PESELCell);
            memberTable.addCell(erasedReasonCell);
            memberTable.addCell(erasedAdditionalDescriptionCell);

            document.add(memberTable);

        }


        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;

    }

    // raport sędziowania - z zakresu dat
    public FilesEntity getJudgingReportInChosenTime(LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {
        String fileName = "raport sędziowania.pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<MemberEntity> arbiters = memberRepository.findAll().stream().filter(f -> !f.isErased()).filter(f -> f.getMemberPermissions().getArbiterNumber() != null).toList();


        Paragraph title = new Paragraph("Raport sędziowania", font(13, 1));
//        Paragraph date = new Paragraph("Łódź, od " + dateFormat(from) + " do " + dateFormat(to), font(10, 2));

        document.add(title);
//        document.add(date);
        // dla każdego sędziego
        for (MemberEntity arbiter : arbiters) {
            if (arbiter.getHistory().getJudgingHistory().size() > 0) {
                List<JudgingHistoryEntity> judgingHistory = arbiter.getHistory().getJudgingHistory().stream().filter(f -> f.getDate().isAfter(firstDate) && f.getDate().isBefore(secondDate)).toList();
                if (judgingHistory.size() > 0) {
                    Paragraph arbiterP = new Paragraph(arbiter.getFirstName() + arbiter.getSecondName(), font(10, 1));
                    document.add(arbiterP);
                }
                // dodawanie jego sędziowania
                for (int j = 0; j < judgingHistory.size(); j++) {
                    Chunk tournamentIndex = new Chunk((j + 1) + " ", font(10, 0));
                    Chunk tournamentName = new Chunk(judgingHistory.get(j).getName() + " ", font(10, 0));
                    Chunk tournamentDate = new Chunk(judgingHistory.get(j).getDate().format(dateFormat()) + " ", font(10, 0));
                    Chunk tournamentFunction = new Chunk(judgingHistory.get(j).getJudgingFunction() + " ", font(10, 0));
                    Paragraph tournament = new Paragraph();
                    tournament.add(tournamentIndex);
                    tournament.add(tournamentName);
                    tournament.add(tournamentDate);
                    tournament.add(tournamentFunction);
                    document.add(tournament);

                }
            }

        }


        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // rejestr pobytu na strzelnicy - z zakresu dat
    public FilesEntity getEvidenceBookInChosenTime(LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {
        String fileName = "Książka rejestru pobytu na strzelnicy od " + firstDate + " do " + secondDate + ".pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, false, true);
        List<RegistrationRecordEntity> collect = registrationRepo.findAll().stream().filter(f -> f.getDateTime().toLocalDate().isAfter(firstDate.minusDays(1)) && f.getDateTime().toLocalDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(RegistrationRecordEntity::getDateTime).reversed()).toList();


        Paragraph title = new Paragraph("Książka rejestru pobytu na strzelnicy od " + firstDate + " do " + secondDate, font(13, 1));

        document.add(title);
        document.add(new Phrase("\n"));

        float[] col = new float[]{10, 30, 20, 30, 30};
        PdfPTable table = new PdfPTable(col);
        Phrase index = new Phrase("lp", font(10, 0));
        Phrase name = new Phrase("Nazwisko i Imię", font(10, 0));
        Phrase dateTime = new Phrase("Data i godzina wejścia", font(10, 0));
        Phrase addressOrWeaponPermissionNumber = new Phrase("Adres lub pozwolenie na broń", font(10, 0));
        Phrase sign = new Phrase("podpis", font(10, 0));
        PdfPCell cell0 = new PdfPCell(index);
        cell0.setHorizontalAlignment(1);
        PdfPCell cell1 = new PdfPCell(name);
        PdfPCell cell2 = new PdfPCell(dateTime);
        PdfPCell cell3 = new PdfPCell(addressOrWeaponPermissionNumber);
        PdfPCell cell4 = new PdfPCell(sign);
        table.addCell(cell0);
        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);
        table.addCell(cell4);
        // dla każdego rekordu
        for (int i = 0; i < collect.size(); i++) {
            RegistrationRecordEntity record = collect.get(i);
            table.setWidthPercentage(100);
            Phrase recordIndex = new Phrase((i + 1) + " ", font(8, 0));
            Phrase recordName = new Phrase(record.getNameOnRecord() + " ", font(8, 0));
            Phrase recordDateTime = new Phrase(record.getDateTime().toString().replace("T", " ").substring(0, 16) + " ", font(8, 0));
            Phrase recordAddressOrWeaponPermissionNumber = new Phrase(record.getWeaponPermission() != null ? record.getWeaponPermission() + " " : record.getAddress(), font(8, 0));
            cell0 = new PdfPCell(recordIndex);
            cell0.setHorizontalAlignment(1);
            cell1 = new PdfPCell(recordName);
            cell2 = new PdfPCell(recordDateTime);
            cell3 = new PdfPCell(recordAddressOrWeaponPermissionNumber);
            table.addCell(cell0);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            Image image;
            if (record.getImageUUID() != null) {
                image = Image.getInstance(getFile(record.getImageUUID()).getData());
                table.addCell(image);
            } else {
                table.addCell("");

            }


        }
        document.add(table);


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder().name(fileName).data(data).type(String.valueOf(MediaType.APPLICATION_PDF)).size(data.length).build();

        FilesEntity filesEntity = createFileEntity(filesModel);

        File file = new File(fileName);

        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return filesEntity;
    }

    // raport czasu pracy
    public FilesEntity getWorkTimeReport(int year, String month, boolean detailed) throws IOException, DocumentException {
        int reportNumber = 1;
        List<IFile> collect = filesRepository.findAllByNameContains("%" + month.toLowerCase() + "%", "%" + year + "%");
        if (!collect.isEmpty()) {
            reportNumber = collect.stream().max(Comparator.comparing(IFile::getVersion)).orElseThrow(EntityNotFoundException::new).getVersion();
        }

        String fileName = "raport_pracy_" + month.toLowerCase() + "_" + reportNumber + "_" + year + "_" + ".pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, true);

        String finalMonth = month.toLowerCase(Locale.ROOT);
        int pl = number(finalMonth);

        List<WorkingTimeEvidenceEntity> evidenceEntities = new ArrayList<>(workRepo.findAllByStopQuery(year, pl));

        List<UserEntity> userEntityList = evidenceEntities.stream().map(WorkingTimeEvidenceEntity::getUser).distinct().toList();


        float[] pointColumnWidths = {4F, 12F, 12F, 14F, 24F, 48F};
        AtomicInteger pageNumb = new AtomicInteger();
        int fontSize = 10;
        Paragraph newLine = new Paragraph(" ", font(10, 1));
        int finalReportNumber = reportNumber;
        userEntityList.forEach(u -> {
            //tutaj tworzę dokument
            try {
                Paragraph title = new Paragraph("Raport Pracy - " + pl + "/" + year + "/" + finalReportNumber, font(13, 1));
                Paragraph name = new Paragraph(u.getFirstName() + " " + u.getSecondName() + " szczegółowy", font(fontSize, 0));
                if (!detailed) {
                    name = new Paragraph(u.getFirstName() + " " + u.getSecondName(), font(fontSize, 0));
                }
                document.add(title);
                document.add(name);
                document.add(newLine);
                PdfPTable titleTable = new PdfPTable(pointColumnWidths);
                titleTable.setWidthPercentage(100);

                PdfPCell lp = new PdfPCell(new Paragraph("lp", font(fontSize, 0)));
                PdfPCell start = new PdfPCell(new Paragraph("Start", font(fontSize, 0)));
                PdfPCell stop = new PdfPCell(new Paragraph("Stop", font(fontSize, 0)));
                PdfPCell time = new PdfPCell(new Paragraph("Czas pracy", font(fontSize, 0)));
                PdfPCell accepted = new PdfPCell(new Paragraph("Czy Zatwierdzony", font(fontSize, 0)));
                PdfPCell desc = new PdfPCell(new Paragraph("Uwagi", font(fontSize, 0)));
                lp.setFixedHeight(15F);
                titleTable.addCell(lp);
                titleTable.addCell(start);
                titleTable.addCell(stop);
                titleTable.addCell(time);
                titleTable.addCell(accepted);
                titleTable.addCell(desc);


                document.add(titleTable);

                document.add(newLine);

            } catch (DocumentException | IOException ex) {
                ex.printStackTrace();
            }
            List<WorkingTimeEvidenceEntity> userWork = evidenceEntities.stream().filter(f -> f.getUser().equals(u)).sorted(Comparator.comparing(WorkingTimeEvidenceEntity::getStart).reversed()).toList();


            AtomicInteger workSumHours = new AtomicInteger();
            AtomicInteger workSumMinutes = new AtomicInteger();
            for (int i = 0; i < userWork.size(); i++) {
                WorkingTimeEvidenceEntity g = userWork.get(i);
                try {
                    LocalDateTime start = g.getStart();
                    LocalDateTime stop = g.getStop();
                    String workTime = workServ.countTime(start, stop);
                    //do poprawy
                    if (!detailed) {
                        start = workServ.getTime(g.getStart(), true);
                        stop = workServ.getTime(g.getStop(), false);
                        workTime = g.getWorkTime();
                    }
                    int workTimeSumHours;
                    int workTimeSumMinutes;

                    String formatStart = start.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"));
                    String formatStop = stop.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"));

                    if (!detailed) {
                        formatStart = start.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                        formatStop = stop.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                    }
                    workTimeSumHours = sumIntFromString(workTime, 0, 2);
                    workTimeSumMinutes = sumIntFromString(workTime, 3, 5);
                    workSumHours.getAndAdd(workTimeSumHours);
                    workSumMinutes.getAndAdd(workTimeSumMinutes);

                    PdfPTable userTable = new PdfPTable(pointColumnWidths);

                    PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(fontSize, 0)));
                    PdfPCell startCell = new PdfPCell(new Paragraph(formatStart, font(fontSize, 0)));
                    PdfPCell stopCell = new PdfPCell(new Paragraph(formatStop, font(fontSize, 0)));
                    PdfPCell timeCell = new PdfPCell(new Paragraph(workTime.substring(0, 5), font(fontSize, 0)));
                    PdfPCell acceptedCell = new PdfPCell(new Paragraph("oczekuje na zatwierdzenie", font(fontSize, 0)));
                    if (g.isAccepted()) {
                        acceptedCell = new PdfPCell(new Paragraph("tak", font(fontSize, 0)));
                    }
                    String des = "";

                    if (g.isAutomatedClosed()) {
                        des = des.concat("-Zamknięte automatycznie-");
                    }
                    if (g.isToClarify()) {
                        des = des.concat("-Nadgodziny-");
                    }
                    PdfPCell descCell = new PdfPCell(new Paragraph(des, font(fontSize, 0)));
                    userTable.setWidthPercentage(100);

                    userTable.addCell(lpCell);
                    userTable.addCell(startCell);
                    userTable.addCell(stopCell);
                    userTable.addCell(timeCell);
                    userTable.addCell(acceptedCell);
                    userTable.addCell(descCell);

                    document.add(userTable);


                } catch (DocumentException | IOException ex) {
                    ex.printStackTrace();
                }
            }
            try {
                int acquire = workSumMinutes.getAcquire() % 60;
                int acquire1 = workSumMinutes.getAcquire() / 60;
                workSumHours.getAndAdd(acquire1);
                String format = String.format("%02d:%02d", workSumHours.getAcquire(), acquire);
                Paragraph sum = new Paragraph("Suma godzin: " + format, font(fontSize, 1));
                sum.setAlignment(2);
                document.add(sum);
                pageNumb.addAndGet(1);

                Paragraph sign = new Paragraph("Dokument Zatwierdził          ", font(fontSize, 0));
                sign.setAlignment(2);
                Paragraph dots = new Paragraph(".....................................          ", font(fontSize, 0));
                dots.setAlignment(2);
                document.add(newLine);
                document.add(newLine);
                document.add(sign);
                document.add(newLine);
                document.add(dots);
                if (pageNumb.get() < userEntityList.size()) {
                    document.newPage();
                }
                document.resetPageCount();
            } catch (DocumentException | IOException ex) {
                ex.printStackTrace();
            }

        });

        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // lista osób z licencjami
    public FilesEntity generateMembersListWithLicense() throws IOException, DocumentException {

        String fileName = "Lista osób z licencjami.pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, false, true);

        List<MemberEntity> collect = memberRepository.findAllByErasedFalse().stream().filter(f -> f.getClub().getId().equals(1)).filter(f -> f.getLicense().getNumber() != null).filter(f -> f.getLicense().isValid()).toList();

        Paragraph newLine = new Paragraph("\n", font(13, 0));

        Paragraph titleA = new Paragraph("Lista osób z licencjami - OGÓLNA", font(13, 0));
        Paragraph titleB = new Paragraph("Lista osób z licencjami - Młodzież", font(13, 0));
        titleA.setAlignment(1);
        titleB.setAlignment(1);

        document.add(titleA);
        document.add(newLine);
        float[] pointColumnWidths = {4F, 28F, 10F, 14F, 14F, 14F};
        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell licenceNumber = new PdfPCell(new Paragraph("numer licencji", font(12, 0)));
        PdfPCell licenceDate = new PdfPCell(new Paragraph("licencja ważna do", font(12, 0)));
        PdfPCell active = new PdfPCell(new Paragraph("składki", font(12, 0)));
        PdfPCell empty = new PdfPCell(new Paragraph("", font(12, 0)));

        titleTable.setWidthPercentage(100);
        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(licenceNumber);
        titleTable.addCell(licenceDate);
        titleTable.addCell(active);
        titleTable.addCell(empty);

        document.add(titleTable);
        document.add(newLine);

        List<MemberEntity> collect1 = collect.stream().filter(MemberEntity::isAdult).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).collect(Collectors.toList());
        for (int i = 0; i < collect1.size(); i++) {

            MemberEntity memberEntity = collect1.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            PdfPCell licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru().getYear()), font(12, 0)));
            PdfPCell activeCell = new PdfPCell(new Paragraph(memberEntity.isActive() ? "Aktywny" : "Brak składek", font(12, 0)));
            PdfPCell emptyCell = new PdfPCell(new Paragraph("", font(12, 0)));

            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(licenceNumberCell);
            memberTable.addCell(licenceDateCell);
            memberTable.addCell(activeCell);
            memberTable.addCell(emptyCell);

            document.add(memberTable);

        }
        document.newPage();
        document.add(titleB);
        document.add(newLine);
        document.add(titleTable);
        collect1 = collect.stream().filter(f -> !f.isAdult()).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).collect(Collectors.toList());
        for (int i = 0; i < collect1.size(); i++) {

            MemberEntity memberEntity = collect1.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            PdfPCell licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru().getYear()), font(12, 0)));
            PdfPCell activeCell = new PdfPCell(new Paragraph(memberEntity.isActive() ? "Aktywny" : "Brak składek", font(12, 0)));
            PdfPCell emptyCell = new PdfPCell(new Paragraph("", font(12, 0)));

            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(licenceNumberCell);
            memberTable.addCell(licenceDateCell);
            memberTable.addCell(activeCell);
            memberTable.addCell(emptyCell);

            document.add(memberTable);

        }
        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName), null));
        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Nie udało się usunąć pliku tymczasowego: {}", fileName, e);
        }
        return filesEntity;
    }

    // Deklaracja LOK
    public FilesEntity getMembershipDeclarationLOK(String memberUUID) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        PdfGenerationResults pdf = lokMembershipPdfGenerator.generate(member);
        return createFile(pdf.fileName(), pdf.data(), memberUUID);
    }


    private FilesModel getFilesModelPNG(String fileName, byte[] data) {
        return FilesModel.builder().name(fileName).data(data).type(String.valueOf(MediaType.IMAGE_PNG)).size(data.length).build();
    }

    private FilesModel getFilesModelPDF(String fileName, byte[] data, String memberUUID) {
        return FilesModel.builder().name(fileName).belongToMemberUUID(memberUUID).data(data).type(String.valueOf(MediaType.APPLICATION_PDF)).size(data.length).build();
    }

    private Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    private void setAttToDoc(String fileName, Document document, boolean pageEvents, boolean isPageNumberStamp) throws DocumentException, FileNotFoundException {
        document.setMargins(35F, 35F, 35F, 70F);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper(environment, isPageNumberStamp, pageEvents));

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        document.addKeywords("deklaracja lok, ksdziesiątka");
        document.addAuthor("KS DZIESIĄTKA");
        document.addCreator("Igor Żebrowski");
    }

    private int number(String finalMonth) {
        return switch (finalMonth) {
            case "styczeń" -> 1;
            case "luty" -> 2;
            case "marzec" -> 3;
            case "kwiecień" -> 4;
            case "maj" -> 5;
            case "czerwiec" -> 6;
            case "lipiec" -> 7;
            case "sierpień" -> 8;
            case "wrzesień" -> 9;
            case "październik" -> 10;
            case "listopad" -> 11;
            case "grudzień" -> 12;
            default -> 0;
        };
    }

    private Integer sumIntFromString(String sequence, int substringStart, int substringEnd) {
        return Integer.parseInt(sequence.substring(substringStart, substringEnd));
    }

    private byte[] convertToByteArray(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }
    /**
     * 1 - BOLD , 2 - ITALIC, 3 - BOLDITALIC
     *
     * @param size  set font size
     * @param style set style Bold/Italic/Bolditalic
     * @return returns new font
     */
    private Font font(int size, int style) throws IOException, DocumentException {
        BaseFont czcionka = BaseFont.createFont("font/times.ttf", BaseFont.IDENTITY_H, BaseFont.CACHED);
        return new Font(czcionka, size, style);
    }

    private String monthFormat(LocalDate date) {

        String day = String.valueOf(date.getDayOfMonth());
        String month = "";

        if (date.getMonth().getValue() == 1) {
            month = "stycznia";
        }
        if (date.getMonth().getValue() == 2) {
            month = "lutego";
        }
        if (date.getMonth().getValue() == 3) {
            month = "marca";
        }
        if (date.getMonth().getValue() == 4) {
            month = "kwietnia";
        }
        if (date.getMonth().getValue() == 5) {
            month = "maja";
        }
        if (date.getMonth().getValue() == 6) {
            month = "czerwca";
        }
        if (date.getMonth().getValue() == 7) {
            month = "lipca";
        }
        if (date.getMonth().getValue() == 8) {
            month = "sierpnia";
        }
        if (date.getMonth().getValue() == 9) {
            month = "września";
        }
        if (date.getMonth().getValue() == 10) {
            month = "października";
        }
        if (date.getMonth().getValue() == 11) {
            month = "listopada";
        }
        if (date.getMonth().getValue() == 12) {
            month = "grudnia";
        }
        String year = String.valueOf(date.getYear());


        return day + " " + month + " " + year;


    }

    @NotNull
    private String getArbiterClass(String arbiterClass) {
        switch (arbiterClass) {
            case "Klasa 3" -> arbiterClass = "Sędzia Klasy Trzeciej";
            case "Klasa 2" -> arbiterClass = "Sędzia Klasy Drugiej";
            case "Klasa 1" -> arbiterClass = "Sędzia Klasy Pierwszej";
            case "Klasa Państwowa" -> arbiterClass = "Sędzia Klasy Państwowej";
            case "Klasa Międzynarodowa" -> arbiterClass = "Sędzia Klasy Międzynarodowej";
            default -> LOG.info("Nie znaleziono Klasy Sędziowskiej");
        }
        return arbiterClass;
    }
    private DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }

    public FilesEntity createFile(String fileName, byte[] data, String memberUUID) {
        FilesModel model = FilesModel.builder().name(fileName).belongToMemberUUID(memberUUID).data(data).type(String.valueOf(MediaType.APPLICATION_PDF)).size(data.length).build();
        return createFileEntity(model);
    }

}

