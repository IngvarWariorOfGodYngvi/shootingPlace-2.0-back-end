package com.shootingplace.shootingplace.file;

import com.google.common.hash.Hashing;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.armory.GunStoreEntity;
import com.shootingplace.shootingplace.armory.GunStoreRepository;
import com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordEntity;
import com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordRepository;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.competition.CompetitionEntity;
import com.shootingplace.shootingplace.competition.CompetitionRepository;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.history.CompetitionHistoryEntity;
import com.shootingplace.shootingplace.history.JudgingHistoryEntity;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import com.shootingplace.shootingplace.otherPerson.OtherPerson;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonService;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
public class FilesService {

    private final MemberRepository memberRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final FilesRepository filesRepository;
    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final GunRepository gunRepository;
    private final ContributionRepository contributionRepository;
    private final CompetitionRepository competitionRepository;
    private final GunStoreRepository gunStoreRepository;
    private final WorkingTimeEvidenceRepository workRepo;
    private final WorkingTimeEvidenceService workServ;
    private final Environment environment;
    private final RegistrationRecordRepository registrationRepo;
    private final OtherPersonService otherPersonService;
    private final UserRepository userRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public FilesService(MemberRepository memberRepository, AmmoEvidenceRepository ammoEvidenceRepository, FilesRepository filesRepository, TournamentRepository tournamentRepository, ClubRepository clubRepository, OtherPersonRepository otherPersonRepository, GunRepository gunRepository, ContributionRepository contributionRepository, CompetitionRepository competitionRepository, GunStoreRepository gunStoreRepository, WorkingTimeEvidenceRepository workRepo, WorkingTimeEvidenceService workServ, Environment environment, RegistrationRecordRepository registrationRepo, OtherPersonService otherPersonService, UserRepository userRepository) {
        this.memberRepository = memberRepository;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.filesRepository = filesRepository;
        this.tournamentRepository = tournamentRepository;
        this.clubRepository = clubRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.gunRepository = gunRepository;
        this.contributionRepository = contributionRepository;
        this.competitionRepository = competitionRepository;
        this.gunStoreRepository = gunStoreRepository;
        this.workRepo = workRepo;
        this.workServ = workServ;
        this.environment = environment;
        this.registrationRepo = registrationRepo;
        this.otherPersonService = otherPersonService;
        this.userRepository = userRepository;
    }

    private FilesEntity createFileEntity(FilesModel filesModel) {
        filesModel.setDate(LocalDate.now());
        filesModel.setTime(LocalTime.now());
        FilesEntity filesEntity = Mapping.map(filesModel);
        LOG.info(filesModel.getName().trim() + " Encja została zapisana");
        return filesRepository.save(filesEntity);

    }

    public void store(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        FilesModel build = FilesModel.builder()
                .name(fileName)
                .type(String.valueOf(file.getContentType()))
                .data(file.getBytes())
                .size(file.getSize())
                .build();
        createFileEntity(build);
    }

    public void uploadCSVOthers(MultipartFile file) throws IOException {

        InputStream inputStream = file.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        List<String> list = br.lines().collect(Collectors.toList());
        list.forEach(e -> {
            String[] s = e.split(";");
            OtherPersonEntity otherPerson = otherPersonService.addPerson(s[2],
                    OtherPerson.builder()
                            .address(new Address())
                            .email("")
                            .phoneNumber("")
                            .memberPermissions(new MemberPermissions())
                            .weaponPermissionNumber("")
                            .secondName(s[0].toUpperCase(Locale.ROOT))
                            .firstName(s[1].substring(0, 1).toUpperCase(Locale.ROOT).concat(s[1].substring(1).toLowerCase(Locale.ROOT)))
                            .build());
            otherPersonRepository.save(otherPerson);
        });

    }

    public List<?> getAllMemberFiles(String uuid) {

        return filesRepository.findAllByBelongToMemberUUIDEquals(uuid);
    }

    public List<?> getAllFilesList(Pageable page) {

        page = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by("date").and(Sort.by("time")).descending());
        return filesRepository.findAllByDateIsNotNullAndTimeIsNotNull(page)
                .stream()
                .collect(Collectors.toList());
    }

    public FilesEntity getFile(String uuid) {
        if (filesRepository.existsById(uuid))
            return filesRepository.getOne(uuid);
        else LOG.warn("plik nie istnieje");
        return null;
    }

    public String store(MultipartFile file, MemberEntity member) throws IOException {
        String name = member.getLegitimationNumber().toString() + member.getSecondName().toUpperCase() + member.getFirstName().toUpperCase();
        FilesModel build = FilesModel.builder()
                .name(name)
                .type(String.valueOf(file.getContentType()))
                .data(file.getBytes())
                .size(file.getSize())
                .belongToMemberUUID(member.getUuid())
                .build();
        FilesEntity fileEntity = createFileEntity(build);

        member.setImageUUID(fileEntity.getUuid());
        memberRepository.save(member);

        return fileEntity.getUuid();
    }

    public ResponseEntity<?> delete(String uuid) {

        if (filesRepository.existsById(uuid)) {

            MemberEntity memberEntity = memberRepository.findAll()
                    .stream()
                    .filter(f -> f.getImageUUID() != null)
                    .filter(f -> f.getImageUUID().equals(uuid))
                    .findFirst()
                    .orElse(null);
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
        GunEntity one = gunRepository.getOne(gunUUID);
        try {
            filesRepository.deleteById(one.getImgUUID());
        } catch (EmptyResultDataAccessException e) {
            System.out.println("plik nie istnieje");
        }
        one.setImgUUID(null);
        gunRepository.save(one);
        return ResponseEntity.ok("usunięto zdjęcie");
    }

    public ResponseEntity<?> countPages() {
        return ResponseEntity.ok(filesRepository.countAllRecordsDividedBy50());
    }

    // podpis rejestr pobytu - klubowicz
    public String storeImageEvidenceBookMember(String imageString, String pesel) {
        String s = imageString.split(",")[1];
        MemberEntity memberEntity = memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> f.getPesel().equals(pesel))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
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
            fullName = otherPersonRepository.getOne(Integer.valueOf(other.getOther().getId())).getFullName();
        } else {
            fullName = other.getOther().getFullName();
        }
        String fileName = fullName + " evidence.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - przyjmowanie amunicji na stan
    public String storeImageAddedAmmo(String imageString, String pinCode) {
        String s = imageString.split(",")[1];
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        String fileName = user.getFullName() + " ammoAdded.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - wydawanie amunicji
    public String storeImageUpkeepAmmo(String imageString, String pinCode) {
        String s = imageString.split(",")[1];
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        String fileName = user.getFullName() + " upkeepAmmo.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // podpis magazyniera - wydawanie bromi
    public String storeImageIssuanceGun(String imageString, String pinCode) {
        String s = imageString.split(",")[1];
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        String fileName = user.getFullName() + " IssuanceGun.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
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
        MemberEntity one = memberRepository.findByLegitimationNumber(memberLeg)
                .orElseThrow(EntityNotFoundException::new);
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

    // podpis magazyniera - wprowadzanie broń na stan
    public String storeImageAddGun(String imageString, String pinCode) {
        String s = imageString.split(",")[1];
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        String fileName = user.getFullName() + " AddGun.png";
        FilesEntity fileEntity = createFileEntity(getFilesModelPNG(fileName, Base64.getMimeDecoder().decode(s)));
        return fileEntity.getUuid();
    }

    // potwierdzenie opłacenie składki
    public FilesEntity contributionConfirm(String memberUUID, String contributionUUID, Boolean a5rotate) throws DocumentException, IOException {
        ClubEntity club = clubRepository.getOne(1);
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        ContributionEntity contributionEntity;
        if (contributionUUID == null || contributionUUID.isEmpty() || contributionUUID.equals("null")) {
            contributionEntity = memberEntity.getHistory().getContributionList().get(0);
        } else {
            contributionEntity = contributionRepository.getOne(contributionUUID);
        }
        int count = (int) memberEntity.getHistory().getContributionList().stream().filter(f -> f.getPaymentDay().equals(contributionEntity.getPaymentDay())).count();
        LocalDate contribution = contributionEntity.getPaymentDay();
        LocalDate validThru = contributionEntity.getValidThru();
        String fileName = "Składka_" + memberEntity.getFullName() + "_" + LocalDate.now().format(dateFormat()) + ".pdf";
        String clubFullName = club.getFullName().toUpperCase();
        a5rotate = a5rotate != null && a5rotate;
        Document document = new Document(a5rotate ? PageSize.A5.rotate() : PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        // używane tylko dla Dziesiątki i TEST
        URL resource = getClass().getClassLoader().getResource("fbg_10_qrcode.jpg");
        assert resource != null;
        Image img = Image.getInstance(resource);
        img.scaleAbsolute(50, 50);
        String group;
        if (memberEntity.getAdult()) {
            group = "OGÓLNA";
        } else {
            group = "MŁODZIEŻOWA";
        }

        String status;
        if (getSex(memberEntity.getPesel()).equals("Pani")) {
            status = "opłaciła";
        } else {
            status = "opłacił";
        }
        String contributionLevel = String.valueOf(90 * count);
        String counter;
        switch (count) {
            case 1:
                counter = "jedną kwartalną składkę członkowską";
                break;
            case 2:
                counter = "dwie kwartalne składki członkowskie";
                break;
            case 3:
                counter = "trzy kwartalne składki członkowskie";
                break;
            case 4:
                counter = "cztery kwartalne składki członkowskie";
                break;
            case 5:
                counter = "pięć kwartalnych składkek członkowskich";
                break;
            case 6:
                counter = "sześć kwartalnych składkek członkowskich";
                break;
            case 7:
                counter = "siedem kwartalnych składkek członkowskich";
                break;
            case 8:
                counter = "osiem kwartalnych składkek członkowskich";
                break;
            case 9:
                counter = "dziewięć kwartalnych składkek członkowskich";
                break;
            case 10:
                counter = "dziesięć kwartalnych składkek członkowskich";
                break;
            default:
                counter = count + " kwartalnych składkek członkowskich";
                break;
        }

        Paragraph newLine = new Paragraph("\n", font(5, 0));

        Paragraph p = new Paragraph(clubFullName + "\n", font(14, 1));
        Paragraph p1 = new Paragraph("Potwierdzenie opłacenia składki członkowskiej", font(11, 2));
        Paragraph h1 = new Paragraph("Grupa ", font(11, 0));
        Phrase h2 = new Phrase(group, font(13, 1));
        Paragraph p2 = new Paragraph("\nNazwisko i Imię : ", font(11, 0));
        Paragraph p211 = new Paragraph("Numer Legitymacji : ", font(11, 0));
        Phrase p3 = new Phrase(memberEntity.getSecondName() + " " + memberEntity.getFirstName(), font(15, 1));
        Phrase p5 = new Phrase(String.valueOf(memberEntity.getLegitimationNumber()), font(13, 1));
        Paragraph p6 = new Paragraph("\nData opłacenia składki : ", font(11, 0));
        Phrase p7 = new Phrase(String.valueOf(contribution), font(11, 1));
        Paragraph p8 = new Paragraph("Składka ważna do : ", font(11, 0));
        Phrase p9 = new Phrase(String.valueOf(validThru), font(11, 1));
        Paragraph p10 = new Paragraph(getSex(memberEntity.getPesel()) + " " + memberEntity.getFullName() + " dnia : " + contribution + " " + status + " " + counter + " w wysokości " + contributionLevel + "zł.", font(11, 0));
        Paragraph p14 = new Paragraph("", font(11, 0));
        Paragraph p16 = new Paragraph("\n", font(15, 0));
        Paragraph p19 = new Paragraph("               pieczęć klubu", font(10, 0));
        Phrase p20 = new Phrase("                                                  ");
        Phrase p21 = new Phrase("podpis osoby przyjmującej składkę");
        Paragraph p200 = new Paragraph("dołącz do naszej grupy na Facebooku", font(9, 0));
        p.setAlignment(1);
        p1.setAlignment(1);
        h1.add(h2);
        h1.setAlignment(1);
        p2.add(p3);
        p2.add("                                    ");
        p211.add(p5);
        p6.add(p7);
        p8.add(p9);
        p20.add(p21);
        p19.add(p20);
        p16.setIndentationLeft(25);
        p19.setIndentationLeft(40);
        document.add(p);
        document.add(p1);
        document.add(h1);
        document.add(p2);
        document.add(p211);
        document.add(p6);
        document.add(p8);
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
            document.add(p10);
        }
        document.add(p14);
        document.add(p16);
        document.add(p19);
        document.add(newLine);
        document.add(newLine);
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
            document.add(img);
            document.add(p200);
        }
        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    // karta członkowska
    public FilesEntity personalCardFile(String memberUUID) throws IOException, DocumentException {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        ClubEntity club = clubRepository.getOne(1);
        String fileName = "Karta Członkowska " + memberEntity.getFirstName().stripTrailing() + " " + memberEntity.getSecondName().stripTrailing() + ".pdf";
        LocalDate birthDate = birthDay(memberEntity.getPesel());
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        String s = "", s1 = "", s2 = "", s3 = "", s4 = "", s5 = "";
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
            s = "Klubu Strzeleckiego „Dziesiątka” Ligi Obrony Kraju w Łodzi";
            s1 = "Klub Strzelecki „Dziesiątka”";
            s2 = "biuro@ksdziesiatka.pl";
            s3 = "Stowarzyszenie Liga Obrony Kraju mające siedzibę główną w Warszawie pod adresem: ";
            s4 = "ul. Chocimska 14, 00-791 Warszawa";
            s5 = "KS „Dziesiątka” LOK Łódź";
        }
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
            s = "Klubu Strzeleckiego RCS Panaszew w Panaszewie";
            s1 = "Klubu Strzeleckiego RCS Panaszew";
            s2 = "biuro@rcspanaszew.pl";
            s3 = "Stowarzyszenie Klub Strzelecki RCS Panaszew mające siedzibę w Panaszewie pod adresem: ";
            s4 = "ul. Panaszew 4A, 99-200 Poddębice";
            s5 = "KS „RCS” Panaszew";
        }
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.MECHANIK.getName())) {
            s = "Uczniowskiego Klubu Sportowego Mechanik w Tomaszowie Mazowieckim";
            s1 = "Uczniowski Klub Sportowy Mechanik w TOmaszowie Mazowieckim";
            s2 = "biuro@uksmechanik.com";
            s3 = "Uczniowski Klub Sportowy MECHANIK mający siedzibę w Tomaszowie Mazowieckim pod adresem: ";
            s4 = "ul. Św. Antoniego 29, 93-200 Tomaszów Mazowiecki";
            s5 = "UKS „Mechanik” Tomaszów Mazowiecki";
        }
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();


        String statement = "Oświadczenie:\n" +
                "- Zobowiązuję się do przestrzegania Regulaminu Strzelnicy, oraz Regulaminu " + s + ".\n" +
                "- Wyrażam zgodę na przesyłanie mi informacji przez " + s1 + " za pomocą środków komunikacji elektronicznej, w szczególności pocztą elektroniczną oraz w postaci sms-ów/mms-ów.\n" +
                "Wyrażenie zgody jest dobrowolne i może być odwołane w każdym czasie w na podstawie oświadczenia skierowanego na adres siedziby Klubu, na podstawie oświadczenia przesłanego za pośrednictwem poczty elektronicznej na adres: " + s2 + " lub w inny uzgodniony sposób.\n" +
                "- Zgadzam się na przetwarzanie moich danych osobowych (w tym wizerunku) przez Administratora Danych, którym jest " + s3 + "\n" + s4 + " w celach związanych z moim członkostwem w " + s5 + ".";


        String sex = getSex(memberEntity.getPesel());
        if (sex.equals("Pani")) {
            sex = "córki";
        } else {
            sex = "syna";
        }
        String adultAcceptation = "- Wyrażam zgodę na udział i członkostwo " + sex + " w Klubie";
        Paragraph p = new Paragraph(club.getFullName() + "\n", font(14, 1));
        p.setAlignment(1);
        String group;
        if (memberEntity.getAdult()) {
            group = "OGÓLNA";
        } else {
            group = "MŁODZIEŻOWA";
        }
        Paragraph p1 = new Paragraph("Karta Członkowska\n", font(13, 2));
        Phrase p2 = new Phrase(group, font(14, 1));
        Paragraph p3 = new Paragraph("\nNazwisko i Imię : ", font(11, 0));
        Phrase p4 = new Phrase(memberEntity.getSecondName() + " " + memberEntity.getFirstName(), font(18, 1));
        Phrase p5 = new Phrase("Numer Legitymacji : ", font(11, 0));
        Phrase p6 = new Phrase(String.valueOf(memberEntity.getLegitimationNumber()), font(18, 1));
        Paragraph p7 = new Paragraph("\nData Wstąpienia : ", font(11, 0));
        Phrase p8 = new Phrase(String.valueOf(memberEntity.getJoinDate()), font(15, 0));
        Paragraph p9 = new Paragraph("Data Urodzenia : ", font(11, 0));
        Phrase p10 = new Phrase(String.valueOf(birthDate), font(11, 0));
        Paragraph p11 = new Paragraph("PESEL : " + memberEntity.getPesel(), font(11, 0));
        Paragraph p12 = new Paragraph("", font(11, 0));
        Phrase p13 = new Phrase(memberEntity.getIDCard());
        String phone = memberEntity.getPhoneNumber();
        String split = phone.substring(0, 3) + " ";
        String split1 = phone.substring(3, 6) + " ";
        String split2 = phone.substring(6, 9) + " ";
        String split3 = phone.substring(9, 12) + " ";
        String phoneSplit = split + split1 + split2 + split3;
        Paragraph p14 = new Paragraph("Telefon Kontaktowy : " + phoneSplit, font(11, 0));
        Paragraph p15 = new Paragraph("", font(11, 0));
        Paragraph p16 = new Paragraph("Adres Zamieszkania", font(11, 0));
        Paragraph p17 = new Paragraph("", font(11, 0));
        Paragraph p18 = new Paragraph(statement, font(11, 0));
        Paragraph p18_5 = new Paragraph("\n\n" + ((memberEntity.getSignBy() != null ? memberEntity.getSignBy() : "") + "                       " + "\n\n"), font(11, 0));
        p18_5.setAlignment(2);
        Paragraph p19;
        if (!memberEntity.getAdult()) {
            if (LocalDate.now().minusYears(18).isBefore(birthDate)) {
                p18 = new Paragraph("\n\n" + statement + "\n" + adultAcceptation + "\n\n     Podpis Rodzica / Opiekuna Prawnego\n         ..................................................", font(11, 0));
            }
            p19 = new Paragraph("\n\n.............................................", font(9, 0));
        } else {
            String city = environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź, " : "Panaszew, ";
            p19 = new Paragraph(city + " " + LocalDate.now() + "..............................", font(9, 0));
        }
        Phrase p20 = new Phrase("                                                                                                 ");
        Phrase p21 = new Phrase(".............................................");
        Paragraph p22 = new Paragraph("miejscowość, data i podpis Klubowicza", font(11, 0));
        Phrase p23 = new Phrase("                                                                 ");
        Phrase p24 = new Phrase("podpis przyjmującego");

        p1.add("Grupa ");
        p1.add(p2);
        p1.setAlignment(1);
        p3.add(p4);
        p3.add("          ");
        p5.add(p6);
        p3.add(p5);
        p7.add(p8);
        p9.add(p10);
        if (memberEntity.getAdult()) {
            p12.add("Numer Dowodu Osobistego : ");
        } else {
            p12.add("Numer Legitymacji Szkolnej / Numer Dowodu Osobistego : ");
        }
        p12.add(p13);
        if (memberEntity.getEmail() != null) {
            p15.add("Email : " + memberEntity.getEmail());
        } else {
            p15.add("Email : Nie podano");
        }
        if (memberEntity.getAddress().getPostOfficeCity() != null) {
            p17.add("Miasto : " + memberEntity.getAddress().getPostOfficeCity() + "\n");
        } else {
            p17.add("Miasto : \n");
        }
        if (memberEntity.getAddress().getZipCode() != null) {
            p17.add("Kod pocztowy : " + memberEntity.getAddress().getZipCode() + "\n");
        } else {
            p17.add("Kod pocztowy : \n");
        }
        if (memberEntity.getAddress().getStreet() != null) {
            String streetNumber = memberEntity.getAddress().getStreetNumber() != null ? memberEntity.getAddress().getStreetNumber() : "";
            p17.add("Ulica : " + streetNumber + "\n");
        } else {
            p17.add("Ulica : \n");
        }
        if (memberEntity.getAddress().getFlatNumber() != null) {
            p17.add("Numer Mieszkania : " + memberEntity.getAddress().getFlatNumber() + "\n");
        } else {
            p17.add("Numer Mieszkania : \n");
        }
        p20.add(p21);
        p19.add(p20);
        p19.setIndentationLeft(40);
        p22.setIndentationLeft(25);
        p22.add(p23);
        p22.add(p24);

        document.add(p);
        document.add(p1);
        document.add(p3);
        document.add(p7);
        document.add(p9);
        document.add(p11);
        document.add(p12);
        document.add(p14);
        document.add(p15);
        document.add(p16);
        document.add(p17);
        document.add(p18);
        document.add(p18_5);
        document.add(p19);
        document.add(p22);

        document.close();

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));
        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    // lista amunicyjna
    public FilesEntity createAmmunitionListDocument(String ammoEvidenceUUID) throws IOException, DocumentException {
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.getOne(ammoEvidenceUUID);
        ClubEntity club = clubRepository.getOne(1);

        List<AmmoInEvidenceEntity> ammoInEvidenceEntityList1 = new ArrayList<>();

        List<AmmoInEvidenceEntity> a = ammoEvidenceEntity.getAmmoInEvidenceEntityList();

        String[] sort = {"5,6mm", "9x19mm", "12/76", ".357", ".38", "7,62x39mm"};

        AmmoInEvidenceEntity ammoInEvidenceEntity1 = a.stream().filter(f -> f.getCaliberName().equals(sort[0])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity2 = a.stream().filter(f -> f.getCaliberName().equals(sort[1])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity3 = a.stream().filter(f -> f.getCaliberName().equals(sort[2])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity4 = a.stream().filter(f -> f.getCaliberName().equals(sort[3])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity5 = a.stream().filter(f -> f.getCaliberName().equals(sort[4])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity6 = a.stream().filter(f -> f.getCaliberName().equals(sort[5])).findFirst().orElse(null);

        List<AmmoInEvidenceEntity> ammoInEvidenceEntityList2 = a.stream().filter(f ->
                        !f.getCaliberName().equals(sort[0])
                                && !f.getCaliberName().equals(sort[1])
                                && !f.getCaliberName().equals(sort[2])
                                && !f.getCaliberName().equals(sort[3])
                                && !f.getCaliberName().equals(sort[4])
                                && !f.getCaliberName().equals(sort[5]))
                .collect(Collectors.toList());

        if (ammoInEvidenceEntity1 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity1);
        }
        if (ammoInEvidenceEntity2 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity2);
        }
        if (ammoInEvidenceEntity3 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity3);
        }
        if (ammoInEvidenceEntity4 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity4);
        }
        if (ammoInEvidenceEntity5 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity5);
        }
        if (ammoInEvidenceEntity6 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity6);
        }

        ammoInEvidenceEntityList1.addAll(ammoInEvidenceEntityList2);

        a.sort(Comparator.comparing(AmmoInEvidenceEntity::getCaliberName));

        String fileName = "Lista_Amunicyjna_" + ammoEvidenceEntity.getDate().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();


        Paragraph number = new Paragraph(ammoEvidenceEntity.getNumber(), font(10, 4));
        Paragraph p = new Paragraph(club.getFullName() + "\n", font(14, 1));
        Paragraph p1 = new Paragraph("Lista rozliczenia amunicji " + ammoEvidenceEntity.getDate().format(dateFormat()), font(12, 2));

        number.setIndentationLeft(450);
        p.setAlignment(1);
        p1.add("\n");
        p1.setAlignment(1);

        document.add(number);
        document.add(p);
        document.add(p1);
        for (AmmoInEvidenceEntity ammoInEvidenceEntity : ammoInEvidenceEntityList1) {
            Paragraph p2 = new Paragraph("Kaliber : " + ammoInEvidenceEntity.getCaliberName() + "\n", font(12, 1));
            p2.add("\n");
            p2.setIndentationLeft(230);
            document.add(p2);
            float[] pointColumnWidths = {20F, 255F, 25};
            PdfPTable tableLabel = new PdfPTable(pointColumnWidths);
            PdfPCell cellLabel = new PdfPCell(new Paragraph(new Paragraph("lp.", font(10, 2))));
            PdfPCell cell1Label = new PdfPCell(new Paragraph(new Paragraph("Imię i Nazwisko", font(10, 2))));
            PdfPCell cell2Label = new PdfPCell(new Paragraph(new Paragraph("ilość sztuk", font(10, 2))));


            tableLabel.addCell(cellLabel);
            tableLabel.addCell(cell1Label);
            tableLabel.addCell(cell2Label);
            document.add(tableLabel);
            for (int j = 0; j < ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().size(); j++) {
                PdfPTable table = new PdfPTable(pointColumnWidths);
                PdfPCell cell;
                PdfPCell cell1;
                PdfPCell cell2;

                String name;
                if (ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getMemberEntity() == null) {
                    OtherPersonEntity otherPersonEntity = ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getOtherPersonEntity();
                    name = otherPersonEntity.getSecondName() + " " + otherPersonEntity.getFirstName();
                } else {
                    MemberEntity memberEntity = ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getMemberEntity();
                    name = memberEntity.getSecondName() + " " + memberEntity.getFirstName();
                }

                cell = new PdfPCell(new Paragraph(String.valueOf(j + 1), font(10, 2)));
                cell1 = new PdfPCell(new Paragraph(name, font(10, 2)));
                cell2 = new PdfPCell(new Paragraph(ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getCounter().toString(), font(10, 2)));
                table.addCell(cell);
                table.addCell(cell1);
                table.addCell(cell2);
                document.add(table);
            }
            PdfPTable tableSum = new PdfPTable(pointColumnWidths);
            PdfPCell cellTableSum = new PdfPCell(new Paragraph(new Paragraph("", font(10, 2))));
            PdfPCell cellTableSum1 = new PdfPCell(new Paragraph(new Paragraph("Suma", font(10, 2))));
            PdfPCell cellTableSum2 = new PdfPCell(new Paragraph(new Paragraph(ammoInEvidenceEntity.getQuantity().toString(), font(10, 2))));
            cellTableSum.setBorder(0);
            cellTableSum1.setBorder(0);
            cellTableSum1.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            tableSum.addCell(cellTableSum);
            tableSum.addCell(cellTableSum1);
            tableSum.addCell(cellTableSum2);
            document.add(tableSum);

        }

        document.close();

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    // wniosek o przedłużenie licencji zawodniczej
    public FilesEntity createApplicationForExtensionOfTheCompetitorsLicense(String memberUUID) throws IOException, DocumentException {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);

        String fileName = "Wniosek " + memberEntity.getFullName() + ".pdf";
        // dlacego to się uzupełnia 2 razy?
        Document document = new Document(PageSize.A4);
        document.setMargins(35F, 35F, 50F, 50F);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        PdfContentByte cb = writer.getDirectContent();
        String path1 = "Wniosek_o_przedluzenie_licencji_zawodniczej.pdf";
        PdfReader reader = new PdfReader(path1);
        PdfImportedPage page = writer.getImportedPage(reader, 1);

        document.newPage();
        cb.addTemplate(page, 0, 0);

        String licenseNumber = memberEntity.getLicense().getNumber();
        char[] P = memberEntity.getPesel().toCharArray();
        String name = memberEntity.getSecondName().toUpperCase() + "  " + memberEntity.getFirstName().toUpperCase();
        String phone = memberEntity.getPhoneNumber();
        String split = phone.substring(0, 3) + " ";
        String split1 = phone.substring(3, 6) + " ";
        String split2 = phone.substring(6, 9) + " ";
        String split3 = phone.substring(9, 12) + " ";
        String phoneSplit = split + split1 + split2 + split3;
        String date = memberEntity.getLicense().getValidThru().toString().substring(2, 4);

        // brać uprawnienia z patentu
        int licenceYear = memberEntity.getLicense().getValidThru().getYear();

        List<CompetitionHistoryEntity> collectPistol = memberEntity.getHistory().getCompetitionHistory()
                .stream()
                .filter(f -> f.getDate().getYear() == licenceYear)
                .filter(CompetitionHistoryEntity::isWZSS)
                .filter(f -> f.getDiscipline() != null)
                .filter(f -> f.getDiscipline().equals(Discipline.values()[0].getName()))
                .collect(Collectors.toList());
        List<CompetitionHistoryEntity> collectRifle = memberEntity.getHistory().getCompetitionHistory()
                .stream()
                .filter(f -> f.getDate().getYear() == licenceYear)
                .filter(CompetitionHistoryEntity::isWZSS)
                .filter(f -> f.getDiscipline() != null)
                .filter(f -> f.getDiscipline().equals(Discipline.values()[1].getName()))
                .collect(Collectors.toList());
        List<CompetitionHistoryEntity> collectShotgun = memberEntity.getHistory().getCompetitionHistory()
                .stream()
                .filter(f -> f.getDate().getYear() == licenceYear)
                .filter(CompetitionHistoryEntity::isWZSS)
                .filter(f -> f.getDiscipline() != null)
                .filter(f -> f.getDiscipline().equals(Discipline.values()[2].getName()))
                .collect(Collectors.toList());

        List<CompetitionHistoryEntity> competitionHistory = memberEntity.getHistory().getCompetitionHistory();
        for (CompetitionHistoryEntity entity : competitionHistory) {
            if (entity.getDisciplineList() != null) {
                if (entity.getDate().getYear() == licenceYear) {
                    for (String s : entity.getDisciplineList()) {
                        if (s.equals(Discipline.PISTOL.getName())) {
                            collectPistol.add(entity);
                        }
                        if (s.equals(Discipline.RIFLE.getName())) {
                            collectRifle.add(entity);
                        }
                        if (s.equals(Discipline.SHOTGUN.getName())) {
                            collectShotgun.add(entity);
                        }
                    }
                }
            }
        }

        int pistol = 0;
        if (memberEntity.getShootingPatent().getPistolPermission()) {
            pistol = collectPistol.size();
        }
        int rifle = 0;
        if (memberEntity.getShootingPatent().getRiflePermission()) {
            rifle = collectRifle.size();
        }
        int shotgun = 0;
        if (memberEntity.getShootingPatent().getShotgunPermission()) {
            shotgun = collectShotgun.size();
        }

        if (pistol >= 4) {
            if (rifle > 2) {
                rifle = 2;
            }
            if (shotgun > 2) {
                shotgun = 2;
            }
            pistol = 4;

        } else if (rifle >= 4) {
            if (pistol > 2) {
                pistol = 2;
            }
            if (shotgun > 2) {
                shotgun = 2;
            }
            rifle = 4;

        } else if (shotgun >= 4) {
            if (pistol > 2) {
                pistol = 2;
            }
            if (rifle > 2) {
                rifle = 2;
            }
            shotgun = 4;

        }

        Paragraph patentNumber = new Paragraph(memberEntity.getShootingPatent().getPatentNumber() + "                                                       " + licenseNumber, font(12, 0));

        patentNumber.setIndentationLeft(160);

        Paragraph newLine = new Paragraph("\n", font(7, 0));
        Paragraph pesel = new Paragraph(P[0] + "   " + P[1] + "   " + P[2] + "   " + P[3] + "   " + P[4] + "   " + P[5] + "  " + P[6] + "   " + P[7] + "   " + P[8] + "   " + P[9] + "   " + P[10] + "                                             " + phoneSplit, font(12, 0));
        Paragraph names = new Paragraph(name, font(12, 0));
        Paragraph year = new Paragraph(date, font(12, 1));
        pesel.setIndentationLeft(72);
        names.setIndentationLeft(150);
        year.setIndentationLeft(350);

        for (int i = 0; i < 11; i++) {

            document.add(newLine);
        }

        document.add(patentNumber);

        for (int i = 0; i < 1; i++) {

            document.add(newLine);
        }
        document.add(pesel);
        document.add(new Paragraph("\n", font(3, 0)));
        for (int i = 0; i < 1; i++) {

            document.add(newLine);
        }
        document.add(names);
        for (int i = 0; i < 5; i++) {

            document.add(newLine);
        }
        document.add(year);

        for (int i = 0; i < 3; i++) {

            document.add(newLine);
        }
        ClubEntity club = clubRepository.getOne(1);
        int fontSize = 10;
        float fixedHeight = 27F;
        document.add(new Phrase("\n", font(7, 0)));
        int counter = 0;
        String activeProfile = environment.getActiveProfiles()[0];
        for (int i = 0; i < pistol; i++) {
            float[] pointColumnWidths = {50, 20, 20, 5, 10, 2, 28};
            PdfPTable table = new PdfPTable(pointColumnWidths);
            counter++;
            PdfPCell cell = new PdfPCell(new Paragraph(collectPistol.get(i).getName() + "\n" + club.getShortName(), font(fontSize, 0)));
            PdfPCell cell1 = new PdfPCell(new Paragraph(" " + collectPistol.get(i).getDate().toString(), font(fontSize, 0)));
            PdfPCell cell2 = new PdfPCell(new Paragraph(activeProfile.equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : activeProfile.equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "", font(fontSize, 0)));
            PdfPCell cell3 = new PdfPCell(new Paragraph("X", font(fontSize, 1)));
            PdfPCell cell4 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell5 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell6 = new PdfPCell(new Paragraph("WZSS", font(fontSize, 0)));
            cell.setBorder(0);
            cell1.setBorder(0);
            cell2.setBorder(0);
            cell3.setBorder(0);
            cell4.setBorder(0);
            cell5.setBorder(0);
            cell6.setBorder(0);
            cell.setUseDescender(true);
            cell.setFixedHeight(fixedHeight);
            cell1.setFixedHeight(fixedHeight);
            cell2.setFixedHeight(fixedHeight);
            cell3.setFixedHeight(fixedHeight);
            cell4.setFixedHeight(fixedHeight);
            cell5.setFixedHeight(fixedHeight);
            cell6.setFixedHeight(fixedHeight);
            table.addCell(cell);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            document.add(table);
            if (counter == 4) {
                document.add(new Phrase("\n", font(3, 0)));
            }
        }
        for (int i = 0; i < rifle; i++) {
            float[] pointColumnWidths = {50, 20, 20, 5, 2, 10, 28};
            PdfPTable table = new PdfPTable(pointColumnWidths);
            counter++;

            PdfPCell cell = new PdfPCell(new Paragraph(collectRifle.get(i).getName() + "\n" + club.getShortName(), font(fontSize, 0)));
            PdfPCell cell1 = new PdfPCell(new Paragraph(" " + collectRifle.get(i).getDate().toString(), font(fontSize, 0)));
            PdfPCell cell2 = new PdfPCell(new Paragraph(activeProfile.equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : activeProfile.equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "", font(fontSize, 0)));
            PdfPCell cell3 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell4 = new PdfPCell(new Paragraph("X", font(fontSize, 1)));
            PdfPCell cell5 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell6 = new PdfPCell(new Paragraph("WZSS", font(fontSize, 0)));
            cell.setBorder(0);
            cell1.setBorder(0);
            cell2.setBorder(0);
            cell3.setBorder(0);
            cell4.setBorder(0);
            cell5.setBorder(0);
            cell6.setBorder(0);
            cell.setUseDescender(true);
            cell.setFixedHeight(fixedHeight);
            cell1.setFixedHeight(fixedHeight);
            cell2.setFixedHeight(fixedHeight);
            cell3.setFixedHeight(fixedHeight);
            cell4.setFixedHeight(fixedHeight);
            cell5.setFixedHeight(fixedHeight);
            cell6.setFixedHeight(fixedHeight);
            table.addCell(cell);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            document.add(table);
            if (counter == 4) {
                document.add(new Phrase("\n", font(3, 0)));
            }
        }
        for (int i = 0; i < shotgun; i++) {
            float[] pointColumnWidths = {50, 20, 20, 8, 3, 6, 28};
            PdfPTable table = new PdfPTable(pointColumnWidths);
            counter++;

            PdfPCell cell = new PdfPCell(new Paragraph(collectShotgun.get(i).getName() + "\n" + club.getShortName(), font(fontSize, 0)));
            PdfPCell cell1 = new PdfPCell(new Paragraph(" " + collectShotgun.get(i).getDate().toString(), font(fontSize, 0)));
            PdfPCell cell2 = new PdfPCell(new Paragraph(activeProfile.equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : activeProfile.equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "", font(fontSize, 0)));
            PdfPCell cell3 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell4 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell5 = new PdfPCell(new Paragraph("X", font(fontSize, 1)));
            PdfPCell cell6 = new PdfPCell(new Paragraph("WZSS", font(fontSize, 0)));
            cell.setBorder(0);
            cell1.setBorder(0);
            cell2.setBorder(0);
            cell3.setBorder(0);
            cell4.setBorder(0);
            cell5.setBorder(0);
            cell6.setBorder(0);
            cell.setUseDescender(true);
            cell.setFixedHeight(fixedHeight);
            cell1.setFixedHeight(fixedHeight);
            cell2.setFixedHeight(fixedHeight);
            cell3.setFixedHeight(fixedHeight);
            cell4.setFixedHeight(fixedHeight);
            cell5.setFixedHeight(fixedHeight);
            cell6.setFixedHeight(fixedHeight);
            table.addCell(cell);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            document.add(table);
            if (counter == 4) {
                document.add(new Phrase("\n", font(3, 0)));
            }
        }
        document.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .belongToMemberUUID(memberUUID)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    // zaświadczenie z Klubu RCS Panaszew
    public FilesEntity CertificateOfClubMembership(String memberUUID, String reason, boolean enlargement) throws IOException, DocumentException {
        MemberEntity member = memberRepository.getOne(memberUUID);
        String fileName = reason + " " + member.getFullName() + ".pdf";

        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);

        Paragraph newLine = new Paragraph("\n", font(12, 0));

        String[] choice = {"ZAŚWIADCZENIE ZWYKŁE", "ZAŚWIADCZENIE DO WPA",};
        Paragraph date = new Paragraph((environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "") + ", " + LocalDate.now().format(dateFormat()), font(12, 0));
        date.setAlignment(2);
        document.add(date);
        Paragraph title = new Paragraph("\n\nZaświadczenie\n", font(14, 1));
        title.setAlignment(1);
        Paragraph subTitle1 = new Paragraph("O CZŁONKOSTWIE W STOWARZYSZENIU \n", font(13, 1));
        subTitle1.setAlignment(1);
        Paragraph subTitle2 = new Paragraph("O CHARAKTERZE STRZELECKIM I KOLEKCJONERSKIM\n\n", font(13, 1));
        subTitle2.setAlignment(1);
        String pesel = "";
        if (!reason.equals(choice[0])) {
            pesel = " PESEL: " + member.getPesel();
        }
        String sex, address;
        if (getSex(member.getPesel()).equals("Pani")) {
            sex = "Pani";
            address = "zamieszkała ";
        } else {
            sex = "Pan";
            address = "zamieszkały ";
        }
        Paragraph par1 = new Paragraph("Niniejszym zaświadczam, że " + sex + " " + member.getFullName() + " " + address + member.getAddress().toString() + ", nr. " + pesel + ", numer legitymacji klubowej: " + member.getLegitimationNumber() + ", " + " jest członkiem Stowarzyszenia Klub Strzelecki RCS Panaszew.", font(12, 0));
        par1.setFirstLineIndent(40);
        Paragraph par2 = new Paragraph("Niniejsze zaświadczenie stanowi potwierdzenie spełnienia jednego z warunków koniecznych do wydania pozwolenia na broń do celów sportowych i/lub kolekcjonerskich, o których mowa w Art. 10 ust. 3 pkt. 3 i 5 Ustawy z dnia 21 maja 1999r. o broni i amunicji (Dz.U. 1999 nr 53 poz. 549).", font(12, 0));
        par2.setFirstLineIndent(40);
        Paragraph par21 = new Paragraph("Zaświadczenie wydaje się na wniosek zainteresowanego.", font(12, 0));
        par21.setFirstLineIndent(40);
        Paragraph par3 = new Paragraph("W załączeniu przekazujemy potwierdzony za zgodność wyciąg ze statutu Stowarzyszenia.", font(12, 0));
        par3.setFirstLineIndent(40);
        Paragraph par4 = new Paragraph("....................................................", font(12, 0));
        par4.setAlignment(2);
        par4.setIndentationRight(40);
        Paragraph par5 = new Paragraph("Podpis członka Zarządu", font(12, 0));
        par5.setAlignment(2);
        par5.setIndentationRight(60);
        document.add(title);
        document.add(subTitle1);
        document.add(subTitle2);
        document.add(par1);
        if (reason.equals(choice[1])) {
            document.add(par2);
            document.add(par21);
            document.add(par3);
        }
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        document.add(par4);
        document.add(par5);

        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));
        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    // wniosek o pozwolenie na broń
    public FilesEntity ApplicationForFirearmsLicense(String memberUUID, String thirdName, String birthPlace, String fatherName, String motherName, String motherMaidenName, String issuingAuthority, LocalDate parseIDDate, LocalDate parselicenseDate, String city) throws DocumentException, IOException {

        MemberEntity member = memberRepository.getOne(memberUUID);

        String fileName = "Wiosek o pozwolenie na broń " + member.getFullName() + ".pdf";
        String policeCity;
        switch (city) {
            case "Białystok":
                policeCity = "w Białymstoku";
                break;
            case "Bydgoszcz":
                policeCity = "w Bydgoszczy";
                break;
            case "Gdańsk":
                policeCity = "w Gdańsku";
                break;
            case "Gorzów Wielkopolski":
                policeCity = "w Gorzowie Wielkopolskim";
                break;
            case "Katowice":
                policeCity = "w Katowicach";
                break;
            case "Kielce":
                policeCity = "w Kielcach";
                break;
            case "Kraków":
                policeCity = "w Krakowie";
                break;
            case "Lublin":
                policeCity = "w Lublinie";
                break;
            case "Olsztyn":
                policeCity = "w Olsztynie";
                break;
            case "Opole":
                policeCity = "w Opolu";
                break;
            case "Poznań":
                policeCity = "w Poznaniu";
                break;
            case "Rzeszów":
                policeCity = "w Rzeszowie";
                break;
            case "Szczecin":
                policeCity = "w Szczecinie";
                break;
            case "Warszawa":
                policeCity = "w Warszawie";
                break;
            case "Wrocław":
                policeCity = "we Wrocławiu";
                break;
            case "Łódź":
            default:
                policeCity = "w Łodzi";
        }

        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, true);
        Paragraph newLine = new Paragraph("\n", font(10, 0));

        Paragraph date = new Paragraph((environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "") + ", " + LocalDate.now().format(dateFormat()), font(10, 0));
        date.setAlignment(2);
        document.add(date);
        Paragraph memberNames = new Paragraph(member.getFirstName() + ' ' + thirdName + ' ' + member.getSecondName(), font(10, 0));
        memberNames.setAlignment(0);
        Paragraph parentsNames = new Paragraph(fatherName + ' ' + motherName + ' ' + motherMaidenName, font(10, 0));
        parentsNames.setAlignment(0);
        Paragraph birthDateAndPlace = new Paragraph((birthDay(member.getPesel()).format(dateFormat())) + ' ' + birthPlace, font(10, 0));
        parentsNames.setAlignment(0);
        Paragraph zipCodeAndCity = new Paragraph(member.getAddress().getZipCode() + ' ' + member.getAddress().getPostOfficeCity(), font(10, 0));
        zipCodeAndCity.setAlignment(0);
        Paragraph phoneNumber = new Paragraph(member.getPhoneNumber(), font(10, 0));
        phoneNumber.setAlignment(0);
        Paragraph emailP = new Paragraph(member.getEmail(), font(10, 0));
        emailP.setAlignment(0);
        Paragraph peselP = new Paragraph(member.getPesel(), font(10, 0));
        peselP.setAlignment(0);
        Paragraph IDCard = new Paragraph(member.getIDCard(), font(10, 0));
        IDCard.setAlignment(0);
        Paragraph issuing = new Paragraph(issuingAuthority, font(10, 0));
        issuing.setAlignment(0);
        Paragraph IDDate = new Paragraph(parseIDDate.format(dateFormat()), font(10, 0));
        IDCard.setAlignment(0);
        Paragraph recipient = new Paragraph("KOMENDANT WOJEWÓDZKI POLICJI", font(13, 1));
        recipient.setAlignment(2);
        Paragraph recipient1 = new Paragraph(policeCity, font(13, 1));
        recipient1.setAlignment(2);
        recipient1.setIndentationRight(80);
        Paragraph title = new Paragraph("PODANIE", font(13, 1));
        title.setAlignment(1);
        Paragraph par1 = new Paragraph("Niniejszym wnoszę o wydanie mi (w postaci decyzji administracyjnej):\n", font(10, 0));
        par1.setFirstLineIndent(30);
        Paragraph par2 = new Paragraph("1.   pozwolenia na posiadanie broni palnej sportowej w łącznej ilości 6 egzemplarzy do celów sportowych, w tym broni:\n", font(10, 0));
        par2.setFirstLineIndent(20);
        Paragraph par21 = new Paragraph("a. bocznego zapłonu z lufami gwintowanymi, o kalibrze do 6 mm,\n" +
                "b. centralnego zapłonu z lufami gwintowanymi, o kalibrze do 12mm,\n" +
                "c. gładko lufowej,\n", font(10, 0));
        par21.setIndentationLeft(50);
        Paragraph par3 = new Paragraph("2.   pozwolenia na posiadanie broni palnej sportowej w łącznej ilości 10 egzemplarzy do celów kolekcjonerskich.", font(10, 0));
        par3.setFirstLineIndent(20);
        Paragraph par4 = new Paragraph("3.   dopuszczenia do posiadania broni palnej sportowej (A,H,I,J,L) podczas uczestnictwa, organizacji lub przeprowadzania strzeleckich zawodów sportowych.", font(10, 0));
        par4.setIndentationLeft(20);
        Paragraph title1 = new Paragraph("UZASADNIENIE", font(13, 1));
        title1.setAlignment(1);
        Paragraph par5 = new Paragraph("Po mojej stronie nie występują żadne negatywne przesłanki uniemożliwiające posiadanie pozwolenia na broń, a ponadto przedstawiam ważną przyczynę posiadania broni, którą jest:", font(10, 0));
        par5.setFirstLineIndent(20);
        Paragraph par6 = new Paragraph("1.   dla broni do celów sportowych:", font(10, 0));
        par6.setFirstLineIndent(20);
        Paragraph par61 = new Paragraph("a. członkostwo w stowarzyszeniu o charakterze strzeleckim tj. w Stowarzyszenie Strzelecko-Kolekcjonerskie RCS Panaszew 99-200 Poddębice Panaszew 4A NIP:8281419076 REGON:388545546 KRS:0000907099\n" +
                "b. posiadanie kwalifikacji sportowych o których mowa w art. 10b UoBiA tj. patentu strzeleckiego \n" +
                member.getShootingPatent().getPatentNumber() + " w dyscyplinach: " + getDisciplinesFromShootingPatentOrLicense(member.getShootingPatent(), null) + "\n" +
                "c. posiadanie ważnej licencji zawodniczej Polskiego Związku Strzelectwa Sportowego NR L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " Z dnia " + parselicenseDate.format(dateFormat()) + "r. w dyscyplinach: " + getDisciplinesFromShootingPatentOrLicense(null, member.getLicense()) + ",\n", font(10, 0));
        par61.setIndentationLeft(50);
        Paragraph par7 = new Paragraph("2.   dla broni sportowej do celów kolekcjonerskich:", font(10, 0));
        par7.setFirstLineIndent(20);
        Paragraph par71 = new Paragraph("a. członkostwo w stowarzyszeniu o charakterze kolekcjonerskim tj. w Stowarzyszenie Strzelecko-Kolekcjonerskie RCS Panaszew 99-200 Poddębice Panaszew 4A NIP:8281419076 REGON:388545546 KRS:0000907099\n" +
                "b. posiadanie kwalifikacji sportowych o których mowa w art. 10b UoBiA tj. patentu strzeleckiego NR " + member.getShootingPatent().getPatentNumber() + " z dnia " + member.getShootingPatent().getDateOfPosting().format(dateFormat()) + " r. w dyscyplinach: " + getDisciplinesFromShootingPatentOrLicense(member.getShootingPatent(), null) + "\n" +
                "c. posiadanie ważnej licencji zawodniczej Polskiego Związku Strzelectwa Sportowego L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " z dnia " + parselicenseDate.format(dateFormat()) + "r. w dyscyplinach: pistolet, karabin, strzelba gładkolufowa,\n", font(10, 0));
        par71.setIndentationLeft(50);
        Paragraph par8 = new Paragraph("3.   dla dopuszczenia do posiadania broni palnej sportowej:", font(10, 0));
        par8.setFirstLineIndent(20);
        Paragraph par81 = new Paragraph("spełnienie kryteriów, o których mowa w art. 30 ust. 1a ustawy o broni i amunicji, tj.  posiadanie ważnej licencji zawodnika strzelectwa sportowego nadanej przez Polski Związek Strzelectwa Sportowego nr L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " Z dnia " + parselicenseDate.format(dateFormat()) + "r.  w dyscyplinach: pistolet, karabin, strzelba gładkolufowa,", font(10, 0));
        par81.setIndentationLeft(50);
        Paragraph par9 = new Paragraph("Na podstawie posiadanych kwalifikacji sportowych tj. posiadania patentu strzeleckiego oraz ważnej licencji wydanych przez PZSS jestem zwolniony z egzaminu przed organem Policji, w zakresie broni sportowej, ponieważ zdałem go na podstawie odrębnych przepisów. ", font(10, 0));
        par9.setFirstLineIndent(20);
        Paragraph par10 = new Paragraph("Strzelectwo sportowe zamierzam uprawiać uczestnicząc we współzawodnictwie w ramach PZSS, jak również poza strukturami Związku. Zamierzam rozwijać się w tym sporcie i chcę strzelać z różnych rodzajów broni palnej sportowej, w wielu konkurencjach i dyscyplinach. Nie posiadając własnej broni nie jestem w stanie startować w planowanych przeze mnie konkurencjach.", font(10, 0));
        par10.setFirstLineIndent(20);
        Paragraph par11 = new Paragraph("Stowarzyszenie strzeleckie, którego jestem członkiem organizuje zawody w kilkudziesięciu różnych konkurencjach strzeleckich, rozgrywanych z różnego rodzaju broni, na różnych dystansach i w różnych warunkach. Są to zarówno konkurencje statyczne, jak i dynamiczne. Chcę brać udział w rywalizacji w dużej części tych konkurencji. W tej liczbie między innymi:\n", font(10, 0));
        par11.setFirstLineIndent(20);
        Paragraph par12 = new Paragraph("1. Karabin centralnego zapłonu, 50m, kategoria MANUAL\n" +
                "2. Karabin centralnego zapłonu, 50m, kategoria OPEN\n" +
                "3. Pistolet centralnego zapłonu, 25m, 10 strzałów stojąc, tarcza TS/2\n" +
                "4. Strzelba dynamiczna, kategoria STANDARD\n" +
                "5. Strzelba dynamiczna, kategoria SEMI-AUTO\n" +
                "6. Strzelba dynamiczna IPSC\n" +
                "7. Pistolet dynamiczny (IPSC), kategoria PRODUCTION\n" +
                "8. Pistolet dynamiczny (IPSC), kategoria STANDARD/MINOR\n" +
                "9. Pistolet dynamiczny (IPSC), kategoria STANDARD/MAJOR\n", font(10, 0));
        par12.setIndentationLeft(20);
        Paragraph par13 = new Paragraph("Zdecydowałem się pominąć konkurencje podobne lub takie, w których mógłbym na początku używać broni zakupionej też do innej konkurencji.  Niemniej jednak, interesują mnie także pozostałe typy strzelectwa sportowego (np. długodystansowe, dynamiczne, czarnoprochowe) i w przyszłości planuję brać udział w zawodach i konkurencjach je obejmujących. W szczególności interesuje mnie strzelectwo dynamiczno-praktyczne takie jak: IPSC, Liga Sportera czy 3 Gun.\n" +
                "Oferta stowarzyszenia o charakterze strzeleckim do którego należę jest stale poszerzana i umożliwia mi szerokie eksplorowanie pasji strzeleckiej. Specyfika konkurencji, w których już startuje oraz będę startował sprawia, że wnioskowana ilość jednostek broni jest mi niezbędna do startu w nich, treningu oraz poszerzania swoich umiejętności sportowych. Mając bogatą ofertę zawodów sportowych potrzebuję dużej dozy elastyczności w wyborze zakupionej broni. Zamierzam nabywać kolejne egzemplarze, w miarę jak moje plany uprawiania sportu strzeleckiego będą tego wymagały.\n", font(10, 0));
        par13.setFirstLineIndent(20);
        Paragraph par14 = new Paragraph("Zamierzam kolekcjonować broń palną sportową różnych rodzajów, typów i modeli. W chwili obecnej nie jestem w stanie określić po ile egzemplarzy broni każdego rodzaju będę mieć w swojej kolekcji. Dopiero zaczynam realizację pasji kolekcjonerskiej i nie jestem w stanie powiedzieć, w którą stronę będę chciał się rozwinąć w najbliższej przyszłości.\n" +
                "Chcę mieć dużą kolekcję stanowiącą przekrój najpopularniejszych modeli broni palnej. Na początek wielkość kolekcji, która \n" +
                "z natury rzeczy nie jest i nie może być zbiorem zamkniętym, oceniam szacunkowo na liczbę 10 sztuk. I dlatego wnoszę o wydanie pozwolenia na taką właśnie ilość broni.\n", font(10, 0));
        par14.setFirstLineIndent(20);
        Paragraph par15 = new Paragraph("Rezygnuję niniejszym z prawa zapoznania się z aktami przed wydaniem decyzji, jeśli organ Policji dojdzie do wniosku, \n" +
                "że należy wydać decyzję zgodną z moim żądaniem.\n", font(10, 0));
        par15.setFirstLineIndent(20);
        Paragraph par16 = new Paragraph("....................................................", font(10, 0));
        par16.setAlignment(2);
        par16.setIndentationRight(80);
        Paragraph par17 = new Paragraph("(podpis)", font(10, 0));
        par17.setAlignment(2);
        par17.setIndentationRight(100);
        Paragraph par18 = new Paragraph("Załączniki:", font(10, 1));
        par18.setFirstLineIndent(20);
        Paragraph par19 = new Paragraph("1) dowód wniesienia opłaty 242 zł za wydanie pozwolenia na broń do celów sportowych (oryginał),\n" +
                "2) dowód wniesienia opłaty 242 zł za wydanie pozwolenia na broń do celów kolekcjonerskich (oryginał),\n" +
                "3) dowód wniesienia opłaty skarbowej 10 zł za dopuszczenie do broni (oryginał),\n" +
                "4) orzeczenie lekarskie  (oryginał),\n" +
                "5) orzeczenie psychologiczne  (oryginał),\n" +
                "6) zaświadczenie o członkostwie w strzeleckim klubie sportowym: RCS Panaszew 99-200 Poddębice Panaszew 4A NIP:8281419076 REGON:388545546 KRS:0000907099 (oryginał)\n" +
                "7) decyzja nadania patentu strzeleckiego PZSS NR " + member.getShootingPatent().getPatentNumber() + " z dnia " + member.getShootingPatent().getDateOfPosting().format(dateFormat()) + "r. (wydruk)\n" +
                "8) ważna licencja zawodnika L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " Z dnia " + parselicenseDate.format(dateFormat()) + "r. (wydruk)\n" +
                "9) zdjęcia – 4szt. (zdjęcia podpisane na odwrocie) \n" +
                "10) Kserokopia dowodu osobistego (oryginał do wglądu)\n", font(8, 0));
        par19.setFirstLineIndent(0);
        par19.setIndentationLeft(20);
        document.add(memberNames);
        document.add(parentsNames);
        document.add(birthDateAndPlace);
        document.add(zipCodeAndCity);
        document.add(phoneNumber);
        document.add(emailP);
        document.add(peselP);
        document.add(issuing);
        document.add(IDCard);
        document.add(IDDate);
        document.add(recipient);
        document.add(recipient1);
        document.add(title);
        document.add(par1);
        document.add(par2);
        document.add(par21);
        document.add(par3);
        document.add(par4);
        document.add(newLine);
        document.add(title1);
        document.add(newLine);
        document.add(par5);
        document.add(par6);
        document.add(par61);
        document.add(par7);
        document.add(par71);
        document.add(par8);
        document.add(par81);
        document.newPage();
        document.newPage();
        document.add(par9);
        document.add(par10);
        document.add(par11);
        document.add(par12);
        document.add(par13);
        document.add(par14);
        document.add(par15);
        document.add(par16);
        document.add(par17);
        document.add(par18);
        document.add(par19);

        document.close();

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);
        file.delete();
        return filesEntity;
    }

    // zaświadczenie z Klubu Dziesiątka
    public FilesEntity CertificateOfClubMembership(String memberUUID, String reason, String city, boolean enlargement) throws IOException, DocumentException {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        ClubEntity club = clubRepository.getOne(1);
        String fileName = reason + " " + memberEntity.getFullName() + ".pdf";

        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph newLine = new Paragraph("\n", font(12, 0));

        PdfPTable mainTable = new PdfPTable(1);

        document.add(mainTable);
        String[] choice = {"ZAŚWIADCZENIE ZWYKŁE", "BROŃ SPORTOWA DO CELÓW SPORTOWYCH", "BROŃ SPORTOWA DO CELÓW KOLEKCJONERSKICH", "BROŃ CIĘCIWOWA W POSTACI KUSZ"};
        // pobieranie z ustawień
        String policeAddress = "";
        String policeCity = "";
        String policeZipCode = "";
        String policeStreet = "";
        String policeStreetNumber = "";

        if (city.equals("Białystok")) {
            policeCity = "w Białymstoku";
            policeZipCode = "15-369";
            policeStreet = "ul. Bema";
            policeStreetNumber = "4";
        }
        if (city.equals("Bydgoszcz")) {
            policeCity = "w Bydgoszczy";
            policeZipCode = "85-090";
            policeStreet = "al. Powstańców Wielkopolskich";
            policeStreetNumber = "7";
        }
        if (city.equals("Gdańsk")) {
            policeCity = "w Gdańsku";
            policeZipCode = "80-298";
            policeStreet = "ul. Harfowa";
            policeStreetNumber = "60";
        }
        if (city.equals("Gorzów Wielkopolski")) {
            policeCity = "w Gorzowie Wielkopolskim";
            policeZipCode = "66-400";
            policeStreet = "ul. Kwiatowa";
            policeStreetNumber = "10";
        }
        if (city.equals("Katowice")) {
            policeCity = "w Katowicach";
            policeZipCode = "40-038";
            policeStreet = "ul. Lompy";
            policeStreetNumber = "19";
        }
        if (city.equals("Kielce")) {
            policeCity = "w Kielcach";
            policeZipCode = "25-366";
            policeStreet = "ul. Śniadeckich";
            policeStreetNumber = "4";
        }
        if (city.equals("Kraków")) {
            policeCity = "w Krakowie";
            policeZipCode = "31-571";
            policeStreet = "ul. Mogilska";
            policeStreetNumber = "109";
        }
        if (city.equals("Lublin")) {
            policeCity = "w Lublinie";
            policeZipCode = "20-213";
            policeStreet = "ul. Gospodarcza";
            policeStreetNumber = "1b";
        }
        if (city.equals("Łódź")) {
            policeCity = "w Łodzi";
            policeZipCode = "91-048";
            policeStreet = "Lutomierska";
            policeStreetNumber = "108/112";
        }
        if (city.equals("Olsztyn")) {
            policeCity = "w Olsztynie";
            policeZipCode = "10-049";
            policeStreet = "ul. Wincentego Pstrowskiego";
            policeStreetNumber = "3";
        }
        if (city.equals("Opole")) {
            policeCity = "w Opolu";
            policeZipCode = "46-020";
            policeStreet = "ul. Powstańców Śląskich";
            policeStreetNumber = "20";
        }
        if (city.equals("Poznań")) {
            policeCity = "w Poznaniu";
            policeZipCode = "60-844";
            policeStreet = "ul. Kochanowskiego";
            policeStreetNumber = "2a";
        }
        if (city.equals("Rzeszów")) {
            policeCity = "w Rzeszowie";
            policeZipCode = "35-036";
            policeStreet = "ul. Dąbrowskiego";
            policeStreetNumber = "30";
        }
        if (city.equals("Szczecin")) {
            policeCity = "w Szczecinie";
            policeZipCode = "71-710";
            policeStreet = "ul. Bardzińska";
            policeStreetNumber = "1a";
        }
        if (city.equals("Warszawa")) {
            policeCity = "w Warszawie";
            policeZipCode = "00-150";
            policeStreet = "ul. Nowolipie";
            policeStreetNumber = "2";
        }
        if (city.equals("Wrocław")) {
            policeCity = "we Wrocławiu";
            policeZipCode = "50-040";
            policeStreet = "ul. Podwale";
            policeStreetNumber = "31/33";
        }

        if (!club.getId().equals(memberEntity.getClub().getId())) {
            reason = choice[0];
        } else {
            if (reason.equals(choice[3])) {
                policeCity = "w Łodzi";
                policeZipCode = "90-114";
                policeStreet = "ul. Henryka Sienkiewicza";
                policeStreetNumber = "28/30";
                policeAddress = "\nKomendant Miejski Policji " + policeCity + "\n " + policeZipCode + " " + city + ", " + policeStreet + " " + policeStreetNumber;
            } else {
                policeAddress = "\nKomendant Wojewódzki Policji " + policeCity + "\nWydział Postępowań Administracyjnych\n " + policeZipCode + " " + city + ", " + policeStreet + " " + policeStreetNumber;
            }
        }

        Paragraph date = new Paragraph((environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "") + ", " + LocalDate.now().format(dateFormat()), font(12, 0));
        date.setAlignment(2);
        Paragraph police = new Paragraph(policeAddress, font(12, 0));
        police.setAlignment(2);
        document.add(date);
        if (!reason.equals(choice[0])) {
            document.add(police);
        }

        Paragraph title = new Paragraph("\n\nZaświadczenie\n\n", font(14, 1));
        title.setAlignment(1);
        String pesel = "";
        if (!reason.equals(choice[0])) {
            pesel = " PESEL: " + memberEntity.getPesel();
        }
        String sex, word;
        if (getSex(memberEntity.getPesel()).equals("Pani")) {
            sex = "Pani";
            word = "wystąpiła";
        } else {
            sex = "Pan";
            word = "wystąpił";
        }
        Paragraph par1 = new Paragraph("" + sex + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + pesel + " jest czynnym członkiem Klubu Strzeleckiego „Dziesiątka” LOK w Łodzi. Numer legitymacji klubowej: " + memberEntity.getLegitimationNumber() + ". " +
                "Uczestniczy w zawodach i treningach strzeleckich osiągając bardzo dobre wyniki. " +
                "Czynnie uczestniczy w życiu Klubu. ", font(12, 0));
        par1.setFirstLineIndent(40);

        Paragraph par2 = new Paragraph(getSex(memberEntity.getPesel()) + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + " wyraża chęć pogłębiania swojej wiedzy i umiejętności w sporcie strzeleckim przez współzawodnictwo w różnych konkurencjach strzeleckich.", font(12, 0));
        par2.setFirstLineIndent(40);
        String s = "";
        if (memberEntity.getLicense().getValidThru() != null) {
            s = String.valueOf(memberEntity.getLicense().getValidThru().getYear());
        }
        Paragraph par3 = new Paragraph(getSex(memberEntity.getPesel()) + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + " posiada Patent Strzelecki PZSS oraz ważną Licencję Zawodniczą PZSS na rok " + s, font(12, 0));
        par3.setFirstLineIndent(40);

        Paragraph par4 = new Paragraph(club.getFullName() + " jest członkiem Polskiego Związku Strzelectwa Sportowego i posiada Licencję Klubową nr LK-" + club.getLicenseNumber() + ", jest również Członkiem Łódzkiego Związku Strzelectwa Sportowego, zarejestrowany pod numerem ewidencyjnym 6.", font(12, 0));
        par4.setFirstLineIndent(40);
        String s1 = enlargement ? "rozszerzenie pozwolenia" : "pozwolenie";
        switch (reason) {
            case "BROŃ SPORTOWA DO CELÓW SPORTOWYCH":
                reason = "na broń sportową do celów sportowych.";
                break;
            case "BROŃ SPORTOWA DO CELÓW KOLEKCJONERSKICH":
                reason = "na broń sportową do celów kolekcjonerskich.";
                break;
            case "BROŃ CIĘCIWOWA W POSTACI KUSZ":
                reason = "na broń cięciwową w postaci kusz.";
                break;
        }
        Paragraph par5 = new Paragraph(getSex(memberEntity.getPesel()) + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + " " + word + " z prośbą o wydanie niniejszego zaświadczenia, skutkiem którego będzie złożenie wniosku o " + s1 + " " + reason, font(12, 0));
        par5.setFirstLineIndent(40);

        Paragraph par6 = new Paragraph("Sporządzono w 2 egz.", font(12, 0));

        Paragraph par7 = new Paragraph("Egz. Nr 1 – a/a\n" +
                "Egz. Nr 2 – Adresat", font(12, 0));
        par7.setIndentationLeft(40);
        document.add(title);
        document.add(par1);
        if (!reason.equals(choice[0])) {
            document.add(par2);
        }
        if (memberEntity.getLicense().getNumber() != null && memberEntity.getMemberPermissions().getArbiterNumber() != null && !reason.equals(choice[0])) {
            document.add(par3);
        }
        if (!reason.equals(choice[0])) {
            document.add(par4);
            document.add(par5);
        }
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        document.add(par6);
        document.add(newLine);
        document.add(newLine);
        document.add(par7);

        document.close();

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    // plik .csv klubowicza
    public FilesEntity getMemberCSVFile(String memberUUID) throws IOException {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        String fileName = memberEntity.getFirstName().stripTrailing() + memberEntity.getSecondName().toUpperCase().stripTrailing() + ".csv";
        File file = new File(fileName);

        String[] tab = new String[5];

        LocalDate localDate = birthDay(memberEntity.getPesel());
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

        FileWriter fileWriter = new FileWriter(fileName);
        String coma = ";";
        for (String s : tab) {

            fileWriter.write(s + coma);
        }

        fileWriter.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .belongToMemberUUID(memberUUID)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        file.delete();
        return filesEntity;
    }

    // plik .csv maile do klubowiczów
    public FilesEntity getMemberEmailCSV() throws IOException {
        List<MemberEntity> all = memberRepository.findAllByErasedFalse();
        String fileName = LocalDate.now() + " mailing.csv";

        StringBuilder csvContent = new StringBuilder();

        all.forEach(e -> csvContent.append(e.getEmail()).append(",").append("\n"));
        FileWriter fw = new FileWriter(fileName);
        try {
            fw.write(String.valueOf(csvContent));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        fw.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .belongToMemberUUID(null)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik " + fileName);

        return filesEntity;
    }

    // metryczka
    public FilesEntity getStartsMetric(String memberUUID, String otherID, String tournamentUUID, List<String> competitions, String startNumber, Boolean a5rotate) throws IOException, DocumentException {
        String name;
        String club;

        if (otherID != null) {
            OtherPersonEntity otherPersonEntity = otherPersonRepository.getOne(Integer.parseInt(otherID));
            name = otherPersonEntity.getSecondName().toUpperCase(Locale.ROOT) + " " + otherPersonEntity.getFirstName();
            club = otherPersonEntity.getClub().getShortName();
        } else {
            MemberEntity memberEntity = memberRepository.getOne(memberUUID);
            name = memberEntity.getSecondName().toUpperCase(Locale.ROOT) + " " + memberEntity.getFirstName();
            club = memberEntity.getClub().getShortName();
        }
        TournamentEntity tournamentEntity = tournamentRepository.getOne(tournamentUUID);

        ClubEntity clubEntity = clubRepository.getOne(1);

        String fileName = "metryki_" + name + ".pdf";
        Document document = a5rotate ? new Document(PageSize.A5.rotate()) : new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, false);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<String> comp = competitions.stream().map(m -> competitionRepository.getOne(m).getName()).filter(value -> !value.contains(" pneumatyczny ") && !value.contains(" pneumatyczna ")).sorted().collect(Collectors.toList());
        competitions.stream().map(m -> competitionRepository.getOne(m).getName()).filter(competition -> competition.contains(" pneumatyczny ") || competition.contains(" pneumatyczna ")).sorted().forEach(comp::add);
        Paragraph newLine = new Paragraph("\n", font(9, 0));
        for (int j = 0; j < comp.size(); j++) {
            int d = Integer.parseInt(startNumber);
            int finalJ = j;
            ScoreEntity score = tournamentEntity.getCompetitionsList()
                    .stream()
                    .filter(f -> f.getName().equals(comp.get(finalJ)))
                    .findFirst().orElseThrow(EntityNotFoundException::new)
                    .getScoreList()
                    .stream()
                    .filter(f -> f.getMetricNumber() == d)
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            CompetitionEntity competitionEntity = competitionRepository.findByNameEquals(comp.get(finalJ)).orElseThrow(EntityNotFoundException::new);
            int numberOfShots;
            if (competitionEntity.getNumberOfShots() > 10) {
                numberOfShots = 10;
            } else {
                numberOfShots = competitionEntity.getNumberOfShots();
            }
            //  nazwa zawodów, tytuł i data
            Paragraph par1 = new Paragraph(tournamentEntity.getName().toUpperCase() + "    " + clubEntity.getShortName() + " " + tournamentEntity.getDate().format(dateFormat()), font(11, 1));
            par1.setAlignment(1);
            String a = "";
            String b = "";
            if (score.isAmmunition()) {
                a = "A";
                b = "";
            }
            if (!score.isAmmunition() && score.isGun()) {
                b = "B";
            }
            // nazwisko klub i numer startowy
            Chunk nameChunk = new Chunk(name, font(10, 1));
            Chunk clubChunk = new Chunk(" " + club, font(10, 1));
            Chunk numberChunk = new Chunk(a + " " + b + "  Nr. " + startNumber, font(13, 1));
            // nazwa konkurencji
            Paragraph par3 = new Paragraph(comp.get(j), font(10, 1));
            par3.setAlignment(1);

            Paragraph par4 = new Paragraph("Podpis sędziego .............................", font(11, 0));
            Chunk chunk1 = new Chunk("                                   Podpis zawodnika .............................       ", font(11, 0));
            Chunk chunk2 = new Chunk(" Nr. " + startNumber, font(13, 1));

            par4.add(chunk1);
            par4.add(chunk2);
            int numberOfColumns = numberOfShots + 5;
            float[] pointColumnWidths = new float[numberOfColumns];
            if (!competitionEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                pointColumnWidths = new float[6];
            }
            if (competitionEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                for (int i = 0; i < numberOfColumns; i++) {

                    if (i < numberOfColumns - 2) {
                        pointColumnWidths[i] = 20F;
                    } else {
                        pointColumnWidths[i] = 30F;
                    }
                }
            } else {
                Arrays.fill(pointColumnWidths, 25F);
            }
            PdfPTable table = new PdfPTable(pointColumnWidths);
            PdfPTable table1 = new PdfPTable(pointColumnWidths);
            PdfPTable table11 = new PdfPTable(pointColumnWidths);

            float[] t2points = new float[5];

            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
                t2points = new float[]{25F, 25F, 10F, 10F, 10F};
            }
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                t2points = new float[]{33F, 33F, 33F, 0F, 0F};
            }
            PdfPTable table2 = new PdfPTable(t2points);

            table.setWidthPercentage(100F);
            table1.setWidthPercentage(100F);
            table11.setWidthPercentage(100F);

            table2.setWidthPercentage(80F);

            Paragraph p0t2 = new Paragraph(nameChunk);
            Paragraph p1t2 = new Paragraph(clubChunk);
            Paragraph p2t2 = new Paragraph(numberChunk);
            Paragraph p3t2 = new Paragraph("BW", font(8, 0));
            Paragraph p4t2 = new Paragraph("BK", font(8, 0));
            Paragraph p5t2 = new Paragraph(" ", font(12, 0));
            Paragraph p6t2 = new Paragraph(" ", font(12, 0));
            Paragraph p7t2 = new Paragraph(" ", font(12, 0));
            Paragraph p8t2 = new Paragraph(" ", font(12, 0));
            Paragraph p9t2 = new Paragraph(" ", font(12, 0));

            p0t2.setAlignment(0);
            p1t2.setAlignment(0);
            p2t2.setAlignment(0);
            p3t2.setAlignment(0);
            p4t2.setAlignment(0);

            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                p3t2 = new Paragraph(" ", font(10, 0));
                p4t2 = new Paragraph(" ", font(10, 0));
            }

            PdfPCell cell0t2 = new PdfPCell(p0t2);
            PdfPCell cell1t2 = new PdfPCell(p1t2);
            PdfPCell cell2t2 = new PdfPCell(p2t2);
            PdfPCell cell3t2 = new PdfPCell(p3t2);
            PdfPCell cell4t2 = new PdfPCell(p4t2);
            PdfPCell cell5t2 = new PdfPCell(p5t2);
            PdfPCell cell6t2 = new PdfPCell(p6t2);
            PdfPCell cell7t2 = new PdfPCell(p7t2);
            PdfPCell cell8t2 = new PdfPCell(p8t2);
            PdfPCell cell9t2 = new PdfPCell(p9t2);

            cell0t2.setBorderWidth(0);
            cell1t2.setBorderWidth(0);
            cell2t2.setBorderWidth(0);

            cell5t2.setBorderWidth(0);
            cell6t2.setBorderWidth(0);
            cell7t2.setBorderWidth(0);

            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                cell3t2.setBorderWidth(0);
                cell4t2.setBorderWidth(0);
                cell8t2.setBorderWidth(0);
                cell9t2.setBorderWidth(0);
            }

            table2.addCell(cell0t2);
            table2.addCell(cell1t2);
            table2.addCell(cell2t2);
            table2.addCell(cell3t2);
            table2.addCell(cell4t2);
            table2.addCell(cell5t2);
            table2.addCell(cell6t2);
            table2.addCell(cell7t2);
            table2.addCell(cell8t2);
            table2.addCell(cell9t2);

            document.add(par1); //  nazwa zawodów i tytuł
            document.add(new Paragraph("\n", font(4, 0))); // nowa linia
            document.add(table2); // nazwisko klub i numer startowy
            document.add(par3); // nazwa konkurencji
            document.add(new Paragraph("\n", font(4, 0))); // nowa linia
            if (competitionEntity.getCountingMethod().equals(CountingMethod.POJEDYNEK.getName())) {
                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph();
                    if (i == 0) {
                        p = new Paragraph(" ", font(12, 0));
                    }
                    if (i == 1) {
                        p = new Paragraph(" ", font(12, 0));
                    }
                    if (i == 2) {
                        p = new Paragraph(" ", font(12, 0));
                    }
                    if (i == 3) {
                        p = new Paragraph(" ", font(12, 0));
                    }
                    if (i == 4) {
                        p = new Paragraph("UWAGI", font(12, 0));
                    }
                    if (i == 5) {
                        p = new Paragraph("UWAGI", font(12, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                }
                document.add(table); // tytuł tabeli
                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph();
                    if (i == 0) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 1) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 2) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 3) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 4) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 5) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table1.addCell(cell);
                }
                document.add(table1); // ciało tabeli
            }
            if (competitionEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                for (int i = 0; i < numberOfColumns; i++) {
                    Paragraph p = new Paragraph(String.valueOf(i), font(12, 0));
                    if (i == 0) {
                        p = new Paragraph("seria", font(12, 0));
                    }
                    if (i == numberOfColumns - 4) {
                        p = new Paragraph("zw", font(12, 0));
                    }
                    if (i == numberOfColumns - 3) {
                        p = new Paragraph("wew", font(12, 0));
                    }
                    if (i == numberOfColumns - 2) {
                        p = new Paragraph("SUMA", font(12, 0));
                    }
                    if (i == numberOfColumns - 1) {
                        p = new Paragraph("UWAGI", font(12, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                    if (i == numberOfColumns - 1) {
                        document.add(table);
                        break;
                    }
                }
                if (numberOfShots < 10) {
                    for (int i = 0; i < numberOfColumns; i++) {

                        Chunk c = new Chunk(" ", font(26, 0));
                        Paragraph p = new Paragraph(c);
                        PdfPCell cell = new PdfPCell(p);
                        table11.addCell(cell);


                        if (i == numberOfColumns - 1) {
                            document.add(table11);
                            break;
                        }
                    }

                }
                int serial = 0;
                int numberOfRows = (competitionEntity.getNumberOfShots() / 10) + 1;
                for (int i = 0; i < numberOfRows; i++) {
                    for (int k = 0; k < numberOfColumns; k++) {
                        String s = " ";
                        if (i < numberOfRows - 1) {
                            if (k % numberOfColumns == 0) {
                                s = arabicToRomanNumberConverter(++serial);
                            }
                            Chunk c;
                            if (k % numberOfColumns == 0) {
                                c = new Chunk(s, font(10, 0));
                            } else {
                                c = new Chunk(s, font(20, 0));
                            }
                            Paragraph p = new Paragraph(c);
                            PdfPCell cell = new PdfPCell(p);
                            p.setAlignment(1);
                            cell.setHorizontalAlignment(1);
                            cell.setVerticalAlignment(1);
                            table1.addCell(cell);
                        } else {
                            Paragraph p = new Paragraph(s, font(20, 0));
                            PdfPCell cell = new PdfPCell(p);
                            if (k < numberOfColumns - 2) {
                                cell.setBorder(0);
                            }
                            if (k == numberOfColumns - 1) {
                                cell.setBorder(0);
                            }
                            table1.addCell(cell);
                        }
                    }
                }
                document.add(table1);
            }
            if (competitionEntity.getCountingMethod().equals(CountingMethod.DYNAMIKADZIESIATKA.getName()) ||
                    competitionEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName()) ||
                    competitionEntity.getCountingMethod().equals(CountingMethod.IPSC.getName()) ||
                    competitionEntity.getCountingMethod().equals(CountingMethod.TIME.getName())
            ) {
                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph();
                    if (i == 0) {
                        p = new Paragraph("ALFA", font(12, 0));
                    }
                    if (i == 1) {
                        p = new Paragraph("CHARLIE", font(12, 0));
                    }
                    if (i == 2) {
                        p = new Paragraph("DELTA", font(12, 0));
                    }
                    if (i == 3) {
                        p = new Paragraph("PROCEDURY", font(12, 0));
                    }
                    if (i == 4) {
                        p = new Paragraph("CZAS", font(12, 0));
                    }
                    if (i == 5) {
                        p = new Paragraph("UWAGI", font(12, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                }
                document.add(table); // tytuł tabeli
                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph();
                    if (i == 0) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 1) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 2) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 3) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 4) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 5) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table1.addCell(cell);
                }
                document.add(table1); // ciało tabeli
            }
            Paragraph par5 = new Paragraph("_______________________________________________________________________________________", font(12, 0));

//            document.add(newLine);
            document.add(par4);
            document.add(par5);
            if ((j + 1) % 4 == 0) {
                document.newPage();
            }
            if (comp.size() > 1 && a5rotate) {
                if (j < comp.size() - 1) {
                    document.newPage();
                }
            }
        }

        document.close();

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    // lista klubowiczów - grupa wiekowa
    public FilesEntity generateMembersListByAdult(boolean adult) throws IOException, DocumentException {
        String fileName = "Lista_klubowiczów_na_dzień " + LocalDate.now().format(dateFormat()) + ".pdf";
        Document document = new Document(PageSize.A4.rotate());
        setAttToDoc(fileName, document, true, true);
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        List<MemberEntity> all = memberRepository.findAll()
                .stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getAdult().equals(adult))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).collect(Collectors.toList());

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
            if (!memberEntity.getActive()) {
                contributionDateCell.setBackgroundColor(BaseColor.RED);
                contributionValidThruCell.setBackgroundColor(BaseColor.RED);
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
        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();
        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
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
        List<MemberEntity> all = memberRepository.findAll().stream().filter(f -> !f.getErased()).sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl())).collect(Collectors.toList());
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

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
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
        Paragraph date = new Paragraph((environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ?
                "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ?
                "Panaszew" : "") + ", " + monthFormat(tournamentEntity.getDate()), font(10, 2));

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

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    // lista osób do zgłoszenia na policję
    public FilesEntity generateListOfMembersToReportToPolice() throws
            IOException, DocumentException {

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
        List<MemberEntity> memberEntityList = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .filter(f -> f.getClub().getId() == 1)
                .filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, pl()))
                .collect(Collectors.toList());
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

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
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

        List<MemberEntity> members = memberRepository.findAllByErasedFalseAndActiveFalse().stream()
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidDate))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))).collect(Collectors.toList());
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

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
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
            GunStoreEntity gunStoreEntity = gunStoreRepository.findAll()
                    .stream()
                    .filter(f -> f.getUuid().equals(guns.get(finalI)))
                    .findFirst().orElseThrow(EntityNotFoundException::new);
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
            List<GunEntity> collect = gunRepository.findAll()
                    .stream()
                    .filter(f -> f.getGunType().equals(list.get(finalI)))
                    .filter(GunEntity::isInStock)
                    .sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName))
                    .collect(Collectors.toList());
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

        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    // list przewozowy
    public FilesEntity getGunTransportCertificate(List<String> guns, LocalDate date, LocalDate date1) throws
            IOException, DocumentException {
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
        guns.forEach(e -> finalCollect.add(gunRepository.getOne(e)));
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
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
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
        List<MemberEntity> memberEntityList = memberRepository.findAllByErasedTrue().stream()
                .filter(f -> f.getErasedEntity() != null)
                .filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)) && f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1)))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))))
                .collect(Collectors.toList());
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
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        file.delete();
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

        List<MemberEntity> arbiters = memberRepository.findAll()
                .stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getMemberPermissions().getArbiterNumber() != null)
                .collect(Collectors.toList());


        Paragraph title = new Paragraph("Raport sędziowania", font(13, 1));
//        Paragraph date = new Paragraph("Łódź, od " + dateFormat(from) + " do " + dateFormat(to), font(10, 2));

        document.add(title);
//        document.add(date);
        // dla każdego sędziego
        for (MemberEntity arbiter : arbiters) {
            if (arbiter.getHistory().getJudgingHistory().size() > 0) {
                List<JudgingHistoryEntity> judgingHistory = arbiter.getHistory().getJudgingHistory().stream().filter(f -> f.getDate().isAfter(firstDate) && f.getDate().isBefore(secondDate)).collect(Collectors.toList());
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
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);

        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return filesEntity;
    }

    // rejestr pobytu na strzelnicy - z zakresu dat
    public FilesEntity getEvidenceBookInChosenTime(LocalDate firstDate, LocalDate secondDate) throws IOException, DocumentException {
        String fileName = "Książka rejestru pobytu na strzelnicy od " + firstDate + " do " + secondDate + ".pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, false, true);
        List<RegistrationRecordEntity> collect = registrationRepo.findAll().stream().filter(f -> f.getDateTime().toLocalDate().isAfter(firstDate.minusDays(1)) && f.getDateTime().toLocalDate().isBefore(secondDate.plusDays(1))).sorted(Comparator.comparing(RegistrationRecordEntity::getDateTime).reversed()).collect(Collectors.toList());


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
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

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

        List<UserEntity> userEntityList = evidenceEntities
                .stream()
                .map(WorkingTimeEvidenceEntity::getUser)
                .distinct()
                .collect(Collectors.toList());


        float[] pointColumnWidths = {4F, 12F, 12F, 14F, 24F, 48F};
        AtomicInteger pageNumb = new AtomicInteger();
        int fontSize = 10;
        Paragraph newLine = new Paragraph(" ", font(10, 1));
        int finalReportNumber = reportNumber;
        userEntityList.forEach(u ->
                {
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
                    List<WorkingTimeEvidenceEntity> userWork = evidenceEntities
                            .stream()
                            .filter(f -> f.getUser().equals(u))
                            .sorted(Comparator.comparing(WorkingTimeEvidenceEntity::getStart).reversed())
                            .collect(Collectors.toList());


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
                        String format = String.format("%02d:%02d",
                                workSumHours.getAcquire(), acquire);
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

                }
        );

        document.close();
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);
        file.delete();
        return filesEntity;
    }

    // lista osób z licencjami
    public FilesEntity generateMembersListWithLicense() throws IOException, DocumentException {

        String fileName = "Lista osób z licencjami.pdf";
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, false, true);

        List<MemberEntity> collect = memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> f.getClub().getId().equals(clubRepository.getOne(1).getId()))
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> f.getLicense().isValid())
                .collect(Collectors.toList());

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

        List<MemberEntity> collect1 = collect.stream()
                .filter(MemberEntity::getAdult)
                .sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl()))
                .collect(Collectors.toList());
        for (int i = 0; i < collect1.size(); i++) {

            MemberEntity memberEntity = collect1.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            PdfPCell licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru().getYear()), font(12, 0)));
            PdfPCell activeCell = new PdfPCell(new Paragraph(memberEntity.getActive() ? "Aktywny" : "Brak składek", font(12, 0)));
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
        collect1 = collect.stream()
                .filter(f -> !f.getAdult())
                .sorted(Comparator.comparing(MemberEntity::getSecondName, pl()).thenComparing(MemberEntity::getFirstName, pl()))
                .collect(Collectors.toList());
        for (int i = 0; i < collect1.size(); i++) {

            MemberEntity memberEntity = collect1.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            PdfPCell licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru().getYear()), font(12, 0)));
            PdfPCell activeCell = new PdfPCell(new Paragraph(memberEntity.getActive() ? "Aktywny" : "Brak składek", font(12, 0)));
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
        FilesEntity filesEntity = createFileEntity(getFilesModelPDF(fileName, convertToByteArray(fileName)));

        File file = new File(fileName);
        file.delete();
        return filesEntity;
    }

    // Deklaracja LOK
    public FilesEntity getMembershipDeclarationLOK(String uuid) throws DocumentException, IOException {
        MemberEntity member = memberRepository.getOne(uuid);

        String fileName = "Deklaracja Członkowska LOK " + member.getFullName() + ".pdf";
        URL resource = getClass().getClassLoader().getResource("logo_LOK.png");
//        String source = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/shootingplace-1.0/WEB-INF/classes/logo_LOK.png";
        assert resource != null;
        Image img = Image.getInstance(resource);
        int fs = 10;
        int ls = 11;
        Document document = new Document(PageSize.A4);
        setAttToDoc(fileName, document, true, true);

        Paragraph newLine = new Paragraph("\n", font(fs, 0));
        Paragraph page = new Paragraph("1/3", font(fs, 0));
        page.setAlignment(0);
        document.add(page);
        document.addCreator("Igor Żebrowski");
        Paragraph zalacznik = new Paragraph("Zał. nr 1. do uchwały ZG LOK", font(fs, 0));
        Paragraph zalacznik1 = new Paragraph("nr 71/2022 z dn. 28.10.2022r.", font(fs, 0));
        zalacznik.setLeading(ls);
        zalacznik.setAlignment(2);
        zalacznik1.setLeading(ls);
        zalacznik1.setAlignment(2);

        document.add(zalacznik);
        document.add(zalacznik1);

        img.setAlignment(0);
        img.scaleAbsolute(100, 65);


        Paragraph title = new Paragraph("     DEKLARACJA CZŁONKOWSKA", font(20, 1));
        title.setAlignment(1);

        float[] f = {20, 80};

        PdfPTable table = new PdfPTable(f);
        PdfPCell cellc1 = new PdfPCell(img);
        PdfPCell cellc2 = new PdfPCell(title);
        cellc1.setBorder(0);
        cellc2.setBorder(0);
        cellc2.setHorizontalAlignment(1);
        cellc2.setVerticalAlignment(5);
        table.addCell(cellc1);
        table.addCell(cellc2);

        document.add(table);

        float[] f1 = {18, 82};
        PdfPTable table1 = new PdfPTable(f1);

        PdfPCell cellt1c1 = new PdfPCell(new Paragraph("Imię i nazwisko", font(11, 1)));
        PdfPCell cellt1c2 = new PdfPCell(new Paragraph(member.getFirstName().toUpperCase() + " " + member.getSecondName().toUpperCase(), font(11, 0)));
        cellt1c1.setHorizontalAlignment(0);
        cellt1c2.setHorizontalAlignment(1);
        cellt1c1.setVerticalAlignment(5);
        cellt1c2.setVerticalAlignment(5);
        table1.addCell(cellt1c1);
        table1.addCell(cellt1c2);
        table1.completeRow();

        PdfPCell cellt2c1 = new PdfPCell(new Paragraph("Data urodzenia", font(11, 1)));
        PdfPCell cellt2c2 = new PdfPCell();
        cellt2c2.setPadding(0);
        cellt2c2.setBorder(0);
        cellt2c1.setVerticalAlignment(5);
        cellt2c1.setHorizontalAlignment(0);
        table1.addCell(cellt2c1);

        float[] f2 = {20, 10, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        PdfPTable table2 = new PdfPTable(f2);
        PdfPCell cell = new PdfPCell(new Paragraph(birthDay(member.getPesel()).format(dateFormat()), font(11, 0)));
        PdfPCell cell1 = new PdfPCell(new Paragraph("PESEL", font(11, 1)));

        cell.setVerticalAlignment(5);
        cell.setHorizontalAlignment(1);
        cell1.setVerticalAlignment(5);
        cell1.setHorizontalAlignment(1);

        table2.addCell(cell);
        table2.addCell(cell1);
        for (int i = 0; i < member.getPesel().length(); i++) {
            Paragraph p = new Paragraph(member.getPesel().substring(i, i + 1), font(11, 0));
            PdfPCell cell2 = new PdfPCell(p);
            cell2.setVerticalAlignment(5);
            cell2.setHorizontalAlignment(1);
            table2.addCell(cell2);
        }
        table2.setWidthPercentage(100);
        table2.setPaddingTop(0);
        cellt2c2.addElement(table2);
        table1.addCell(cellt2c2);
        table1.completeRow();

        PdfPCell addressTitleCell = new PdfPCell(new Paragraph("Adres \nzamieszkania", font(11, 1)));
        PdfPCell addressCell = new PdfPCell();
        addressTitleCell.setVerticalAlignment(5);
        addressTitleCell.setHorizontalAlignment(0);
        table1.addCell(addressTitleCell);

        PdfPTable table3 = new PdfPTable(1);
        table3.setWidthPercentage(100);
        table3.addCell(new PdfPCell(new Paragraph(member.getAddress().fullAddress(), font(11, 0))));
        table3.completeRow();
        table3.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        addressCell.setBorder(0);
        addressCell.setPadding(0);
        addressCell.addElement(table3);
        table1.addCell(addressCell);
        table1.completeRow();

        PdfPCell cell2 = new PdfPCell(new Paragraph("Posiadane\nodznaczenia\npaństwowe/LOK", font(10, 1)));
        table1.addCell(cell2);
        table1.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        table1.completeRow();

        PdfPCell phoneTitleCell = new PdfPCell(new Paragraph("Numer telefonu", font(11, 1)));
        phoneTitleCell.setHorizontalAlignment(0);
        phoneTitleCell.setVerticalAlignment(5);

        table1.addCell(phoneTitleCell);

        PdfPCell phoneCell = new PdfPCell();
        phoneCell.setBorder(0);
        phoneCell.setPadding(0);
        PdfPTable table4 = new PdfPTable(2);
        table4.setWidthPercentage(100);
        PdfPCell cell3 = new PdfPCell(new Paragraph("stacjonarny", font(9, 0)));
        PdfPCell cell4 = new PdfPCell(new Paragraph("komórkowy", font(9, 0)));
        cell3.setHorizontalAlignment(0);
        cell4.setHorizontalAlignment(0);
        table4.addCell(cell3);
        table4.addCell(cell4);
        table4.completeRow();
        table4.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));

        String phone = member.getPhoneNumber();
        String split = phone.substring(0, 3) + " ";
        String split1 = phone.substring(3, 6) + " ";
        String split2 = phone.substring(6, 9) + " ";
        String split3 = phone.substring(9, 12) + " ";
        String phoneSplit = split + split1 + split2 + split3;

        table4.addCell(new PdfPCell(new Paragraph(phoneSplit, font(11, 0))));
        phoneCell.addElement(table4);
        table1.addCell(phoneCell);
        table1.completeRow();

        PdfPCell emailTitleCell = new PdfPCell(new Paragraph("Adres e-mail", font(11, 1)));
        emailTitleCell.setHorizontalAlignment(0);
        emailTitleCell.setVerticalAlignment(5);
        table1.addCell(emailTitleCell);
        PdfPCell emailCell = new PdfPCell(new Paragraph(member.getEmail(), font(11, 0)));
        emailCell.setHorizontalAlignment(1);
        emailCell.setVerticalAlignment(5);
        table1.addCell(emailCell);
        table1.completeRow();

        document.add(table1);

        Paragraph p = new Paragraph("Proszę o przyjęcie mnie w poczet członków Stowarzyszenia Liga Obrony Kraju.", font(fs, 0));
        p.setIndentationLeft(55);
        p.setLeading(ls);
//        document.add(newLine);
        document.add(p);
        Paragraph p1 = new Paragraph("1.     Po zapoznaniu się ze Statutem Stowarzyszenia Liga Obrony Kraju w szczególności z§ 21, § 22.1,2,\n" +
                "§23, §24.1, §27, §29 i §32 oświadczam, że:", font(fs, 0));
        p1.setIndentationLeft(55);
        Paragraph p1a = new Paragraph("     •   będę godnie reprezentować Ligę obrony kraju, dbać o prestiż i wizerunek stowarzyszenia oraz\n" +
                "propagować jego cele i zadania,", font(fs, 0));
        p1a.setIndentationLeft(65);
        Paragraph p1b = new Paragraph("     •   będę przestrzegać postanowień statutu, regulaminów i uchwał władz stowarzyszenia,", font(fs, 0));
        p1b.setIndentationLeft(65);
        Paragraph p1c = new Paragraph("     •   będę brać czynny udział w pracy klubu (koła) do którego wstępuję, znam jego regulamin, cele i zadania,", font(fs, 0));
        p1c.setIndentationLeft(65);
        Paragraph p1d = new Paragraph("     •   będę opłacać regularnie składkę członkowską i inne świadczenia obowiązujące w stowarzyszeniu.", font(fs, 0));
        p1d.setIndentationLeft(65);
        p1.setLeading(ls);
        p1a.setLeading(ls);
        p1b.setLeading(ls);
        p1c.setLeading(ls);
        p1d.setLeading(ls);
        document.add(p1);
        document.add(p1a);
        document.add(p1b);
        document.add(p1c);
        document.add(p1d);

        String state;

        Paragraph p2 = new Paragraph("2.     Wyrażam zgodę na przetwarzanie moich ww. danych osobowych przez Stowarzyszenie Liga Obrony\n" +
                "     Kraju zgodnie z Rozporządzeniem Parlamentu Europejskiego i Rady (UE) 2016/679 z dn. 27.04.2016r.\n" +
                "     (Rozporządzenie 2016/679).", font(fs, 0));
        p2.setIndentationLeft(55);
        p2.setLeading(ls);
        document.add(p2);
        state = getSex(member.getPesel()).equals("Pan") ? "zostałem poinformowany" : "zostałam poinformowana";
        Paragraph p3 = new Paragraph("3.     Potwierdzam, że " + state + " o tym, że:", font(fs, 0));
        p3.setIndentationLeft(55);
        p3.setLeading(ls);
        document.add(p3);
        Paragraph p3a = new Paragraph("a)     Administratorem podanych danych osobowych jest Stowarzyszenie Liga Obrony Kraju mające siedzibę\n" +
                "główną w Warszawie pod adresem: ul. Chocimska 14, 00-791 Warszawa.", font(fs, 0));
        p3a.setIndentationLeft(65);
        Paragraph p3b = new Paragraph("b)     W Stowarzyszeniu Liga Obrony Kraju wyznaczono inspektora ochrony danych.\n" +
                "     Dane kontaktowe inspektora są następujące:\n" +
                "        - adres korespondencyjny:                    " +
                "          Inspektor Ochrony Danych\n" +
                "                                                                                Liga Obrony Kraju, Biuro Zarządu Głównego\n" +
                "                                                                                ul. Chocimska 14, 00-791 Warszawa\n" +
                "        - adres poczty elektronicznej:               " +
                "          iod@lok.org.pl", font(fs, 0));
        p3b.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "Pana" : "Pani";
        Paragraph p3c = new Paragraph("c)     " + state + " dane będą przekazane w celu:\n" +
                "        - realizacji zadań określonych w Statucie LOK na podstawie art. 6 ust. 1 lit. a) Rozp. 2016/679;\n" +
                "        - udokumentowania organom kontrolującym posiadanych przez " + (getSex(member.getPesel()).equals("Pan") ? "Pana" : "Panią") + " kwalifikacji;\n" +
                "        - wypełnienia obowiązków prawnych ciążących na LOK na podstawie powszechnie obowiązujących\n" +
                "        przepisów prawa, m.in. przepisów podatkowych oraz o rachunkowości, na podstawie\n" +
                "        art. 6 ust. 1 lit, c) Rozp. 2016/679;\n" +
                "        - rozliczenia finansowego zleconych usług, w tym egzekucji należności wynikających z wzajemnej\n" +
                "        umowy, na podstawie art. 6 ust. 1 lit f) Rozp. 2016/679. Prawnie uzasadnionym interesem LOK jest\n" +
                "        zapewnienie odpowiednich dochodów z prowadzonej działalności;\n" +
                "        - badania jakości realizacji usług szkoleniowych na podstawie art. 6 ust. 1 lit f) Rozp. 2016/679.\n" +
                "        Prawnie uzasadnionym interesem LOK jest pozyskanie informacji o poziomie satysfakcji klientów ze\n" +
                "        świadczonych usług;\n" +
                "        - oraz w celach analitycznych i statystycznych na podstawie art, 6 ust. 1 lit f) Rozp. 2016/679.\n" +
                "        Prawnie uzasadnionym interesem LOK jest prowadzenie analizy wyników prowadzonej działalności.", font(fs, 0));
        p3c.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "Pana" : "Pani";
        Paragraph p3d = new Paragraph("d)     " + state + " dane osobowe będą (mogą być) przekazywane:\n" +
                "        - nadrzędnym władzom LOK oraz w niezbędnym zakresie:\n" +
                "           • współpracującym z LOK instytucjom, urzędom administracji państwowej i samorządowej oraz firmom\n" +
                "           w związku z realizacją zadań statutowych;\n" +
                "           • innym podwykonawcom realizującym wspólnie z LOK zadania statutowe;\n" +
                "           • urzędom uprawnionym do nadawania wyróżnień i odznaczeń państwowych;\n" +
                "           • klientom LOK oraz innym członkom LOK, którzy będą chcieli sprawdzić kwalifikacje kadry szkolącej\n" +
                "           realizującej usługę szkoleniową;\n" +
                "           • redakcji Biuletynu Ligi Obrony Kraju \"Czata\";\n" +
                "           • administratorom: stron internetowych, mediów społecznościowych dokumentujących działalność\n" +
                "           klubów i kół LOK w celu np. publikacji wyników z zawodów (imprez);\n" +
                "        - operatorom pocztowym w zakresie niezbędnym do przesyłania korespondencji;\n" +
                "        - bankom w zakresie realizacji płatności;\n" +
                "        - organom publicznym uprawnionym do otrzymania " + state + " danych na podstawie przepisów\n" +
                "        prawa (np. organy wymiaru sprawiedliwości, organy skarbowe, komornicy itd.);", font(fs, 0));
        p3d.setIndentationLeft(65);
        Paragraph p3e = new Paragraph("e)    " + state + " dane osobowe nie będą przekazywane do państwa trzeciego lub organizacji międzynarodowej.\n" +
                "Jeśli w związku z działalnością statutową zajdzie potrzeba przekazania danych za granicę to dane zostaną\n" +
                "przekazane na podstawie odrębnej zgody;", font(fs, 0));
        p3e.setIndentationLeft(65);
        Paragraph p3f = new Paragraph("f)     " + state + " dane będą przechowywane przez okres:\n" +
                "        - bezterminowo - dane opublikowane w Księdze Honorowej lub Kronice Ligi Obrony Kraju;\n" +
                "        - deklaracja członkowska - 5 lat od zakończenia członkostwa w LOK;\n" +
                "        - 5 lat - dokumenty finansowe, podatkowe;\n" +
                "        - do 5 lat pozostałe (zgodnie z odrębnymi regulacjami dla poszczególnych rodzajów dokumentów);", font(fs, 0));
        p3f.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "Panu" : "Pani";
        Paragraph p3g = new Paragraph("g)     Przysługuje " + state + " prawo do:\n" +
                "        - żądania od administratora dostępu do swoich danych osobowych, ich sprostowania, usunięcia lub\n" +
                "        ograniczenia przetwarzania; wniesienia sprzeciwu wobec przetwarzania; przenoszenia danych;", font(fs, 0));
        p3g.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "Pan" : "Pani";
        Paragraph p3h = new Paragraph("h)     Ma " + state + " prawo do cofnięcia zgody na przetwarzanie w dowolnym momencie bez wpływu na\n" +
                "zgodność z prawem przetwarzania, którego dokonano na podstawie zgody przed jej cofnięciem;", font(fs, 0));
        p3h.setIndentationLeft(65);
        Paragraph p3i = new Paragraph("i)     Ma " + state + " prawo do wniesienia skargi do organu nadzorczego zajmującego się ochroną danych\n" +
                "osobowych w Polsce (Prezes Urzędu Ochrony Danych Osobowych), jeśli uzna " + state + ", że jej dane są\n" +
                "przetwarzane z naruszeniem przepisów Rozporządzenia 2016/679 oraz przepisów krajowych dotyczących\n" +
                "ochrony danych osobowych;", font(fs, 0));
        p3i.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "Pana" : "Panią";
        Paragraph p3j = new Paragraph("j)     Podanie przez " + state + " danych jest wymogiem umownym. Konsekwencją niepodania danych\n" +
                "jest odmowa przyjęcia w poczet członków Stowarzyszenia Liga Obrony Kraju;", font(fs, 1));
        p3j.setIndentationLeft(65);
        Paragraph p3k = new Paragraph("k)     Podane przez " + state + " dane osobowe nie będą przetwarzane w systemach automatycznie\n" +
                "podejmujących decyzje, nie będą profilowane.", font(fs, 0));
        p3k.setIndentationLeft(65);
        p3a.setLeading(ls);
        p3b.setLeading(ls);
        p3c.setLeading(ls);
        p3d.setLeading(ls);
        p3e.setLeading(ls);
        p3f.setLeading(ls);
        p3g.setLeading(ls);
        p3h.setLeading(ls);
        p3i.setLeading(ls);
        p3j.setLeading(ls);
        p3k.setLeading(ls);
        document.add(p3a);
        document.add(p3b);
        document.add(p3c);
        document.newPage();
        page = new Paragraph("2/3", font(fs, 0));
        page.setAlignment(0);
        document.add(page);
        document.add(zalacznik);
        document.add(zalacznik1);
        document.add(p3d);
        document.add(p3e);
        document.add(p3f);
        document.add(p3g);
        document.add(p3h);
        document.add(p3i);
        document.add(p3j);
        document.add(p3k);

        Paragraph p4 = new Paragraph("4.     Jednocześnie oświadczam źe:", font(fs, 0));
        p4.setIndentationLeft(55);
        p4.setLeading(ls);
        document.add(p4);

        Paragraph p4a = new Paragraph("a)     zachowam w poufności dane osobowe: klientów LOK, pracowników i członków LOK, otrzymane w związku\n" +
                "z prowadzoną działalnością statutową.\n" +
                "W szczególności nie będę wykorzystywać powierzonych danych osobowych:\n" +
                "do prowadzenia działalności reklamowej usług i produktów własnych i firm trzecich, w celach prywatnych\n" +
                "(np. matrymonialnych), w celu ich \"sprzedaży\" innym osobom, podmiotom gospodarczym;", font(fs, 0));
        p4a.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "przetwarzał" : "przetwarzała";
        Paragraph p4b = new Paragraph("b)     powierzone dane osobowe będę " + state + " tylko w zakresie niezbędnym do prawidłowej realizacji\n" +
                "zadań statutowych, a po ich wykonaniu nie będę danych osobowych przetwarzać dłużej niź jest to\n" +
                "potrzebne lub wymagane przez stosowne przepisy np. podatkowe;", font(fs, 0));
        p4b.setIndentationLeft(65);
        Paragraph p4c = new Paragraph("c)     powierzonych danych osobowych nie będę przekazywał poza granice kraju oraz organizacjom\n" +
                "międzynarodowym;", font(fs, 0));
        p4c.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "realizował" : "realizowała";
        Paragraph p4d = new Paragraph("d)     na żądanie administratora będę niezwłocznie " + state + " żądania osób fizycznych wynikające z praw\n" +
                "określonych w art. 15-22 Rozporządzenia 2016/679;", font(fs, 0));
        p4d.setIndentationLeft(65);
        Paragraph p4e = new Paragraph("e)     jeśli ww. żądanie wpłynie bezpośrednio do mnie, to w ciągu 72h przekażę je inspektorowi ochrony danych;", font(fs, 0));
        p4e.setIndentationLeft(65);
        Paragraph p4f = new Paragraph("f)     zawiadomię w ciągu 48h inspektora ochrony danych o każdym naruszeniu ochrony powierzonych przez\n" +
                "LOK danych osobowych w sposób określony w pkt. 3.8 procedury PW 1.7 ochrony danych osobowych.\n" +
                "Procedura jest dostępna po zalogowaniu na stronie - www.lok.org.pl/iso\n" +
                "dane użytkownika - procedury@lok.org.pl, hasło - procedury);", font(fs, 0));
        p4f.setIndentationLeft(65);
        state = getSex(member.getPesel()).equals("Pan") ? "przestrzegał" : "przestrzegała";
        Paragraph p4g = new Paragraph("g)     będę " + state + " uregulowań dotyczących zachowania poufności danych firmowych określonych\n" +
                "w umowach zawartych ze współpracującymi z LOK firmami. Jeśli takiej umowy nie sporządzono na piśmie\n" +
                "lub nie zawiera ona stosownych regulacji, to zachowam w poufności wobec stron trzecich wszelkie\n" +
                "informacje pozyskane z firm współpracujących z LOK, również po wystąpieniu z LOK.", font(fs, 0));
        p4g.setIndentationLeft(65);
        p4a.setLeading(ls);
        p4b.setLeading(ls);
        p4c.setLeading(ls);
        p4d.setLeading(ls);
        p4e.setLeading(ls);
        p4f.setLeading(ls);
        p4g.setLeading(ls);
        document.add(p4a);
        document.add(p4b);
        document.add(p4c);
        document.add(p4d);
        document.add(p4e);
        document.add(p4f);
        document.add(p4g);
        document.add(newLine);

        PdfPTable table5 = new PdfPTable(2);
        Paragraph p5 = new Paragraph("........................................ dnia " + LocalDate.now().format(dateFormat()), font(fs, 0));
        p5.setLeading(ls);
        PdfPCell cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        Paragraph p6 = new Paragraph("...............................................................", font(fs, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        table5.completeRow();

        p5 = new Paragraph("           (miejscowość)", font(8, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("(czytelny podpis osoby składającej deklarację)    ", font(8, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        document.add(table5);
        document.newPage();
        page = new Paragraph("3/3", font(fs, 0));
        page.setAlignment(0);
        document.add(page);
        document.add(zalacznik);
        document.add(zalacznik1);
        Paragraph title2 = new Paragraph("Oświadczenie opiekuna prawnego *)", !member.getAdult() ? font(15, 1) : font(15, 8));
        title2.setAlignment(1);
        document.add(title2);
        document.add(newLine);

        float[] f3 = {20, 80};
        PdfPTable table7 = new PdfPTable(f3);
        Paragraph p7 = new Paragraph("Imię i nazwisko:", font(11, 1));
        PdfPCell cell7 = new PdfPCell();
        cell7.setHorizontalAlignment(0);
        cell7.setVerticalAlignment(5);
        cell7.addElement(p7);
        table7.addCell(cell7);
        table7.addCell(new PdfPCell());
        table7.completeRow();

        PdfPCell addressTitleCell1 = new PdfPCell(new Paragraph("Adres", font(11, 1)));
        PdfPCell addressCell1 = new PdfPCell();
        addressTitleCell1.setVerticalAlignment(5);
        addressTitleCell1.setHorizontalAlignment(0);
        table7.addCell(addressTitleCell1);

        PdfPTable table8 = new PdfPTable(1);
        table8.setWidthPercentage(100);
        table8.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        table8.completeRow();
        table8.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        addressCell1.setBorder(0);
        addressCell1.setPadding(0);
        addressCell1.addElement(table8);
        table7.addCell(addressCell1);
        table7.completeRow();

        table7.addCell(new PdfPCell(new Paragraph("PESEL", font(11, 1))));
        PdfPCell cell8 = new PdfPCell();

        PdfPTable table9 = new PdfPTable(new float[]{35, 15, 50});
        table9.setWidthPercentage(100);
        table9.addCell(new PdfPCell());
        table9.addCell(new PdfPCell(new Paragraph("Nr. telefonu:", font(11, 1))));
        table9.addCell(new PdfPCell());
        cell8.addElement(table9);
        cell8.setBorder(0);
        cell8.setPadding(0);
        table7.addCell(cell8);
        document.add(table7);
        state = getSex(member.getPesel()).equals("Pan") ? "mojego podopiecznego" : "mojej podopiecznej";
        Paragraph p9 = new Paragraph("     Oświadczam, że wyrażam zgodę na wstąpienie " + state + " do\n" +
                "Stowarzyszenia Liga Obrony Kraju. Akceptuję i potwierdzam wyrażoną w deklaracji członkowskiej przez\n" +
                state + " zgodę dotyczącą przetwarzania danych osobowych. Jednocześnie wyrażam\n" +
                "zgodę na przetwarzanie moich powyżej podanych danych osobowych zgodnie z informacją w pk. 3 deklaracji\n" +
                "członkowskiej. Potwierdzam, że się z tą informacją zapoznałem.", font(fs, 0));
        p9.setIndentationLeft(55);
        p9.setLeading(ls);
        document.add(p9);
        document.add(newLine);
        document.add(newLine);

        table5 = new PdfPTable(2);
        p5 = new Paragraph("........................................ dnia " + LocalDate.now().format(dateFormat()), font(fs, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("...............................................................", font(fs, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        table5.completeRow();

        p5 = new Paragraph("           (miejscowość)", font(8, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("(czytelny podpis opiekuna prawnego)          ", font(8, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        document.add(table5);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);

        PdfPTable line = new PdfPTable(1);
        PdfPCell line1 = new PdfPCell(new Paragraph(" "));
        line1.setBorderWidthLeft(0);
        line1.setBorderWidthRight(0);
        line1.setBorderWidthTop(0);
        line1.setPadding(0);
        line.addCell(line1);
        document.add(line);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        state = getSex(member.getPesel()).equals("Pan") ? " został przyjęty" : " została przyjęta";
        Paragraph p12 = new Paragraph("Potwierdzam, że " + getSex(member.getPesel()) + " " + member.getFullName().toUpperCase() + state + " do:", font(12, 0));
        Paragraph p13 = new Paragraph(clubRepository.getOne(1).getFullName(), font(12, 0));
        Paragraph p14 = new Paragraph("na podstawie uchwały nr: ...................................... z dnia .........................", font(12, 0));
        Paragraph p15 = new Paragraph("Numer legitymacji członkowskiej " + member.getLegitimationNumber(), font(12, 0));
        p12.setIndentationLeft(55);
        p13.setIndentationLeft(55);
        p13.setAlignment(1);
        p14.setIndentationLeft(55);
        p15.setIndentationLeft(55);

        document.add(p12);
        document.add(newLine);
        document.add(newLine);
        document.add(p13);
        document.add(newLine);
        document.add(newLine);
        document.add(p14);
        document.add(p15);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);

        table5 = new PdfPTable(2);
        String city = environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "";
        p5 = new Paragraph(".............. " + city + " .............. dnia ...........................", font(fs, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("...............................................................", font(fs, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        table5.completeRow();

        p5 = new Paragraph("           (miejscowość)", font(8, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("(funkcja w LOK, czytelny podpis)          ", font(8, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        document.add(table5);

        document.close();
        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .belongToMemberUUID(member.getUuid())
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);
        file.delete();
        return filesEntity;
    }


    private FilesModel getFilesModelPNG(String fileName, byte[] data) {
        return FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.IMAGE_PNG))
                .size(data.length)
                .build();
    }

    private FilesModel getFilesModelPDF(String fileName, byte[] data) {
        return FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();
    }

    private Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    private void setAttToDoc(String fileName, Document document, boolean pageEvents, boolean isPageNumberStamp) throws DocumentException, FileNotFoundException {
        document.setMargins(35F, 35F, 35F, 70F);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper(environment, isPageNumberStamp, pageEvents));

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        document.addLanguage("pl");
        document.addKeywords("dekraracja lok, ksdziesiątka");
        document.addAuthor("KS DZIESIĄTKA");
        document.addCreator("Igor Żebrowski");
    }

    private int number(String finalMonth) {
        int pl = 0;
        switch (finalMonth) {
            case "styczeń":
                pl = 1;
                break;
            case "luty":
                pl = 2;
                break;
            case "marzec":
                pl = 3;
                break;
            case "kwiecień":
                pl = 4;
                break;
            case "maj":
                pl = 5;
                break;
            case "czerwiec":
                pl = 6;
                break;
            case "lipiec":
                pl = 7;
                break;
            case "sierpień":
                pl = 8;
                break;
            case "wrzesień":
                pl = 9;
                break;
            case "październik":
                pl = 10;
                break;
            case "listopad":
                pl = 11;
                break;
            case "grudzień":
                pl = 12;
                break;
        }
        return pl;
    }

    private Integer sumIntFromString(String sequence, int substringStart, int substringEnd) {
        return Integer.parseInt(sequence.substring(substringStart, substringEnd));
    }

    private String getSex(String pesel) {
        int i = pesel.charAt(9);
        if (i % 2 == 0) {
            return "Pani";
        } else return "Pan";
    }

    private byte[] convertToByteArray(String path) throws IOException {
        File file = new File(path);
        return Files.readAllBytes(file.toPath());

    }

    /**
     * @return date of birth in format year-month-day
     */
    private LocalDate birthDay(String pesel) {

        int year;
        int month;
        year = 10 * Integer.parseInt(pesel.substring(0, 1));
        year += Integer.parseInt(pesel.substring(1, 2));
        month = 10 * Integer.parseInt(pesel.substring(2, 3));
        month += Integer.parseInt(pesel.substring(3, 4));
        if (month > 80 && month < 93) {
            year += 1800;
        } else if (month > 0 && month < 13) {
            year += 1900;
        } else if (month > 20 && month < 33) {
            year += 2000;
        } else if (month > 40 && month < 53) {
            year += 2100;
        } else if (month > 60 && month < 73) {
            year += 2200;
        }

        if (month > 12 && month < 33) {
            month -= 20;
        }
        int day = Integer.parseInt(pesel.substring(4, 6));

        return LocalDate.of(year, month, day);
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
            case "Klasa 3":
                arbiterClass = "Sędzia Klasy Trzeciej";
                break;
            case "Klasa 2":
                arbiterClass = "Sędzia Klasy Drugiej";
                break;
            case "Klasa 1":
                arbiterClass = "Sędzia Klasy Pierwszej";
                break;
            case "Klasa Państwowa":
                arbiterClass = "Sędzia Klasy Państwowej";
                break;
            case "Klasa Międzynarodowa":
                arbiterClass = "Sędzia Klasy Międzynarodowej";
                break;
            default:
                LOG.info("Nie znaleziono Klasy Sędziowskiej");

        }
        return arbiterClass;
    }

    private String arabicToRomanNumberConverter(int arabicNumber) {
        String romanNumber;
        switch (arabicNumber) {
            case 0:
                romanNumber = "";
                break;
            case 1:
                romanNumber = "I";
                break;
            case 2:
                romanNumber = "II";
                break;
            case 3:
                romanNumber = "III";
                break;
            case 4:
                romanNumber = "IV";
                break;
            case 5:
                romanNumber = "V";
                break;
            case 6:
                romanNumber = "VI";
                break;
            case 7:
                romanNumber = "VII";
                break;
            case 8:
                romanNumber = "VIII";
                break;
            case 9:
                romanNumber = "IX";
                break;
            case 10:
                romanNumber = "X";
                break;
            default:
                romanNumber = "error";
                break;
        }
        return romanNumber;
    }

    private DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }

    private String getPartOfDate(LocalDate date) {
        String month, year;
        month = date.getMonthValue() < 10 ? "0" + date.getMonthValue() : String.valueOf(date.getMonthValue());
        year = String.valueOf(date.getYear());
        return month + "/" + year;
    }

    private String getDisciplinesFromShootingPatentOrLicense(ShootingPatentEntity patent, LicenseEntity license) {
        String pistol = null, rifle = null, shotgun = null;
        if (patent != null) {
            pistol = patent.getPistolPermission() ? "pistolet" : "";
            rifle = patent.getRiflePermission() ? "karabin" : "";
            shotgun = patent.getShotgunPermission() ? "strzelba gładkolufowa" : "";
        }
        if (license != null) {
            pistol = license.isPistolPermission() ? "pistolet" : "";
            rifle = license.isRiflePermission() ? "karabin" : "";
            shotgun = license.isShotgunPermission() ? "strzelba gładkolufowa" : "";
        }
        return pistol + " " + rifle + " " + shotgun;
    }

}

