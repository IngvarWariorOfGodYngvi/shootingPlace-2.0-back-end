package com.shootingplace.shootingplace.member;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.contributions.ContributionService;
import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.enums.ErasedType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.license.LicenseRepository;
import com.shootingplace.shootingplace.license.LicenseService;
import com.shootingplace.shootingplace.member.permissions.MemberPermissionsService;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentService;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LicenseService licenseService;
    private final LicenseRepository licenseRepository;
    private final ShootingPatentService shootingPatentService;
    private final ContributionService contributionService;
    private final HistoryService historyService;
    private final WeaponPermissionService weaponPermissionService;
    private final MemberPermissionsService memberPermissionsService;
    private final ClubRepository clubRepository;
    private final ErasedRepository erasedRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private static final Logger LOG = LogManager.getLogger(MemberService.class);
    private static final Collator PL_COLLATOR = Collator.getInstance(Locale.forLanguageTag("pl"));

    public List<MemberInfo> getArbiters() {
        return memberRepository.findAllByErasedFalseAndMemberPermissions_ArbiterNumberIsNotNull().stream().map(Mapping::map2).sorted(Comparator.comparing(MemberInfo::getSecondName, PL_COLLATOR).thenComparing(MemberInfo::getFirstName, PL_COLLATOR)).toList();
    }

    @Transactional
    public void checkMembers() {

        LOG.info("Sprawdzam składki, licencje oraz przejście do dorosłych");
        historyService.checkStarts();
        List<MemberEntity> members = memberRepository.findAllByErasedFalse();
        LocalDate today = LocalDate.now();
        for (MemberEntity member : members) {
            boolean active = member.getHistory() != null && member.getHistory().getContributionList() != null && !member.getHistory().getContributionList().isEmpty() && !member.getHistory().getContributionList().getFirst().getValidThru().isBefore(today);
            member.setActive(active);
            LicenseEntity license = member.getLicense();
            if (license != null && license.getNumber() != null) {
                license.setValid(!license.getValidThru().isBefore(today));
                licenseRepository.save(license);
            }
            if (!member.isAdult() && member.getBirthDate() != null && member.getBirthDate().plusYears(18).isBefore(today)) {
                LOG.info("Przenoszę do Grupy Ogólnej: {}", member.getFullName());
                member.setAdult(true);
            }
        }

        memberRepository.saveAll(members);
    }


    @Transactional
    public ResponseEntity<?> addNewMember(Member member, Address address, boolean returningToClub, String pinCode) throws NoUserPermissionException {
        // 1. Walidacje unikalności (PESEL, email, legitymacja, dowód)
        List<MemberEntity> allMembers = memberRepository.findAll();
        // PESEL
        MemberEntity byPesel = allMembers.stream().filter(m -> Objects.equals(m.getPesel(), member.getPesel())).findFirst().orElse(null);
        if (byPesel != null) {
            if (returningToClub && Boolean.TRUE.equals(byPesel.isErased())) {
                LOG.info("Ktoś z usuniętych ma taki numer PESEL");
            } else {
                LOG.error("Ktoś już ma taki numer PESEL");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki numer PESEL");
            }
        }
        // email
        String finalEmail = member.getEmail();
        boolean emailTaken = allMembers.stream().map(MemberEntity::getEmail).filter(Objects::nonNull).filter(s -> !s.isEmpty()).anyMatch(email -> email.equals(finalEmail));
        if (emailTaken) {
            if (returningToClub) {
                LOG.info("Ktoś z usuniętych już ma taki e-mail");
            } else {
                LOG.info("Ktoś już ma taki e-mail");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki e-mail");
            }
        }
        // numer legitymacji
        if (member.getLegitimationNumber() != null && memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent()) {
            if (returningToClub) {
                LOG.info("Będzie przyznany nowy numer legitymacji");
            } else {
                boolean existsAmongErased = allMembers.stream().filter(m -> Boolean.TRUE.equals(m.isErased())).anyMatch(m -> Objects.equals(m.getLegitimationNumber(), member.getLegitimationNumber()));
                LOG.error("Ktoś już ma taki numer legitymacji");
                if (existsAmongErased) {
                    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś wśród skreślonych już ma taki numer legitymacji");
                } else {
                    return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki numer legitymacji");
                }
            }
        }

        // numer dowodu
        boolean duplicatedIdCard = allMembers.stream().filter(m -> !Boolean.TRUE.equals(m.isErased())).map(MemberEntity::getIDCard).filter(Objects::nonNull).anyMatch(id -> id.trim().toUpperCase().equals(member.getIDCard()));
        if (duplicatedIdCard) {
            if (returningToClub) {
                LOG.info("Ktoś z usuniętych już ma taki numer dowodu osobistego");
            } else {
                LOG.error("Ktoś już ma taki numer dowodu osobistego");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Ktoś już ma taki numer dowodu osobistego");
            }
        }
        // 2. Uzupełnienie brakujących danych wejściowych
        String email = (member.getEmail() == null || member.getEmail().isEmpty()) ? "" : member.getEmail().toLowerCase();
        LocalDate joinDate = member.getJoinDate() == null ? LocalDate.now() : member.getJoinDate();
        LOG.info("ustawiono " + (member.getJoinDate() == null ? "domyślną " : "") + "datę zapisu na " + joinDate);
        int legitimationNumber;
        if (member.getLegitimationNumber() == null) {
            int number = 1;
            if (!allMembers.isEmpty()) {
                number = allMembers.stream().filter(m -> m.getLegitimationNumber() != null).max(Comparator.comparing(MemberEntity::getLegitimationNumber)).orElseThrow(EntityNotFoundException::new).getLegitimationNumber() + 1;
            }
            legitimationNumber = number;
            LOG.info("ustawiono domyślny numer legitymacji : " + legitimationNumber);
        } else {
            legitimationNumber = member.getLegitimationNumber();
        }
        boolean adult = member.getAdult();
        LOG.info("Klubowicz należy do " + (adult ? "grupy dorosłej" : "grupy młodzieżowej"));
        PersonalEvidence peBuild = PersonalEvidence.builder().ammoList(new ArrayList<>()).build();
        // normalizacja danych osobowych
        member.setFirstName(normalizeFirstName(member.getFirstName()));
        member.setSecondName(member.getSecondName().toUpperCase());
        member.setPhoneNumber(normalizePhone(member.getPhoneNumber()));
        member.setEmail(email);
        member.setJoinDate(joinDate);
        member.setLegitimationNumber(legitimationNumber);
        member.setAdult(adult);
        member.setAddress(address);
        member.setIDCard(member.getIDCard().trim().toUpperCase());
        member.setPesel(member.getPesel());
        member.setClub(clubRepository.findById(1).orElseThrow(EntityNotFoundException::new));
        member.setShootingPatent(shootingPatentService.getShootingPatent());
        member.setLicense(licenseService.getLicense());
        member.setHistory(historyService.getHistory());
        member.setWeaponPermission(weaponPermissionService.getWeaponPermission());
        member.setMemberPermissions(memberPermissionsService.getMemberPermissions());
        member.setPersonalEvidence(peBuild);
        member.setPzss(false);
        member.setErasedEntity(null);
        member.setErased(false);
        member.setActive(true);

        MemberEntity memberEntity = memberRepository.save(Mapping.map(member));

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, memberEntity, HttpStatus.CREATED, "Dodanie Nowego Klubowicza " + member.getFullName(), "nowy Klubowicz");

        if (response.getStatusCode().equals(HttpStatus.CREATED)) {
            UserEntity user = userRepository.findByPinCode(Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString()).orElseThrow(() -> new EntityNotFoundException("Brak użytkownika"));
            MemberGroupEntity groupEntity = memberGroupRepository.findByName(member.getGroup()).orElseThrow(() -> new IllegalStateException("Nie znaleziono grupy: " + member.getGroup()));
            memberEntity.setSignBy(user.getFullName());
            memberEntity.setMemberEntityGroup(groupEntity);
            MemberEntity saved = memberRepository.save(memberEntity);

            emailService.sendRegistrationConfirmation(saved.getUuid());

            historyService.addContribution(memberEntity.getUuid(), contributionService.addFirstContribution(LocalDate.now(), pinCode));

            return ResponseEntity.status(HttpStatus.CREATED.value()).body(memberEntity.getUuid());
        }

        return response;
    }

    @Transactional
    public ResponseEntity<?> activateOrDeactivateMember(String uuid, String pinCode) throws NoUserPermissionException {

        MemberEntity member = memberRepository.findById(uuid).orElse(null);
        if (member == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, member, HttpStatus.OK, "Zmieniono status aktywny/nieaktywny", "Zmieniono status aktywny/nieaktywny");

        if (response.getStatusCode().is2xxSuccessful()) {
            member.toggleActive();
            memberRepository.save(member);
            LOG.info("Zmieniono status dla " + member.getFullName());
        }

        return response;
    }

    @Transactional
    public ResponseEntity<?> changeAdult(String memberUUID, String pinCode) throws NoUserPermissionException {

        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        if (Boolean.TRUE.equals(member.isAdult())) {
            LOG.info("Klubowicz należy już do grupy powszechnej");
            return ResponseEntity.badRequest().body("Klubowicz należy już do grupy powszechnej");
        }

        if (LocalDate.now().minusYears(1).minusDays(1).isBefore(member.getJoinDate())) {
            LOG.info("Klubowicz ma za krótki staż jako młodzież");
            return ResponseEntity.badRequest().body("Klubowicz ma za krótki staż jako młodzież");
        }

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, member, HttpStatus.OK, "Zmieniono grupę na dorosłą", "Klubowicz należy od teraz do grupy dorosłej");

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            member.setAdult(true);
            memberRepository.save(member);
            LOG.info("Klubowicz należy od teraz do grupy dorosłej : " + LocalDate.now());
        }

        return response;
    }

    //--------------------------------------------------------------------------
    // SKREŚLANIE KLUBOWICZA
    //--------------------------------------------------------------------------

    @Transactional
    public ResponseEntity<?> eraseMember(String memberUUID, String erasedType, LocalDate erasedDate, String additionalDescription, String pinCode) throws NoUserPermissionException {

        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        if (!Boolean.TRUE.equals(member.isErased())) {
            ErasedEntity erased = ErasedEntity.builder().erasedType(erasedType).date(erasedDate).additionalDescription(additionalDescription).inputDate(LocalDate.now()).build();

            erasedRepository.save(erased);

            member.setErasedEntity(erased);
            member.toggleErase();
            member.setPzss(false);
            LOG.info("Klubowicz skreślony : " + LocalDate.now());
        }

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, member, HttpStatus.OK, "Usunięto Klubowicza", "Usunięto Klubowicza");

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberRepository.save(member);
        }

        return response;
    }

    //--------------------------------------------------------------------------
    // AKTUALIZACJA DANYCH KLUBOWICZA
    //--------------------------------------------------------------------------

    @Transactional
    public ResponseEntity<?> updateMember(String memberUUID, Member member, String pinCode) throws NoUserPermissionException {

        MemberEntity entity = memberRepository.findById(memberUUID).orElse(null);
        if (entity == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().build();
        }

        // imię
        if (member.getFirstName() != null && !member.getFirstName().isEmpty()) {
            entity.setFirstName(normalizeFirstName(member.getFirstName()));
            LOG.info("Zaktualizowano pomyślnie Imię");
        }

        // nazwisko
        if (member.getSecondName() != null && !member.getSecondName().isEmpty()) {
            entity.setSecondName(member.getSecondName().toUpperCase());
            LOG.info("Zaktualizowano pomyślnie Nazwisko");
        }

        // data przystąpienia
        if (member.getJoinDate() != null) {
            entity.setJoinDate(member.getJoinDate());
            LOG.info("Zaktualizowano pomyślnie Data przystąpienia do klubu");
        }

        // legitymacja
        if (member.getLegitimationNumber() != null) {
            boolean exists = memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();
            if (exists) {
                LOG.warn("Już ktoś ma taki numer legitymacji");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Już ktoś ma taki numer legitymacji");
            } else {
                entity.setLegitimationNumber(member.getLegitimationNumber());
                LOG.info("Zaktualizowano pomyślnie Numer Legitymacji");
            }
        }

        // email
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            boolean emailExists = memberRepository.findByEmail(member.getEmail()).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();

            if (emailExists) {
                LOG.error("Już ktoś ma taki sam e-mail");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Uwaga! Już ktoś ma taki sam e-mail");
            } else {
                entity.setEmail(member.getEmail().trim().toLowerCase());
                LOG.info("Zaktualizowano pomyślnie Email");
            }
        }

        // telefon
        if (member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty()) {
            String normalized = normalizePhone(member.getPhoneNumber());

            if (member.getPhoneNumber().replaceAll("[\\s-]", "").length() != 9) {
                LOG.error("Żle podany numer");
            }

            boolean phoneExists = memberRepository.findByPhoneNumber(normalized).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();
            if (phoneExists) {
                LOG.error("Ktoś już ma taki numer telefonu");
            } else {
                entity.setPhoneNumber(normalized);
                LOG.info("Zaktualizowano pomyślnie Numer Telefonu");
            }
        }

        // dowód
        if (member.getIDCard() != null && !member.getIDCard().isEmpty()) {
            String id = member.getIDCard().trim().toUpperCase();

            boolean idExists = memberRepository.findByIDCard(id).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();
            if (idExists) {
                LOG.error("Ktoś już ma taki numer dowodu");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body("Ktoś już ma taki numer dowodu");
            } else {
                entity.setIDCard(id);
                LOG.info("Zaktualizowano pomyślnie Numer Dowodu");
            }
        }

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, entity, HttpStatus.OK, "update member", "Zaktualizowano dane klubowicza " + entity.getFullName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberRepository.save(entity);
        }
        return response;
    }

    public ResponseEntity<?> getMember(int number) {
        if (!memberRepository.existsByLegitimationNumber(number)) {
            return ResponseEntity.badRequest().body("Klubowicz o podanym numerze legitymacji nie istnieje");
        }
        MemberEntity member = memberRepository.findByLegitimationNumber(number).orElseThrow(EntityNotFoundException::new);
        historyService.checkStarts(member.getUuid());
        LOG.info("Wywołano Klubowicza " + member.getFullName());
        return ResponseEntity.ok(member);
    }

    public MemberEntity getMember(String uuid) {
        return memberRepository.findById(uuid).orElse(null);
    }

    public ResponseEntity<?> getMemberUUIDByLegitimationNumber(int number) {
        if (!memberRepository.existsByLegitimationNumber(number)) {
            return ResponseEntity.badRequest().body("Nie udało się znaleźć takiej osoby");
        }
        String uuid = memberRepository.findByLegitimationNumber(number).orElseThrow(EntityNotFoundException::new).getUuid();
        return ResponseEntity.ok(uuid);
    }

    public List<MemberInfo> getAllNames() {
        return memberRepository.findAllByErasedFalse().stream().map(Mapping::map1).sorted(Comparator.comparing(MemberInfo::getSecondName, PL_COLLATOR).thenComparing(MemberInfo::getFirstName, PL_COLLATOR)).toList();
    }

    public List<MemberDTO> getAdvancedSearch(boolean isErased, int searchType, String inputText) {
        List<MemberEntity> all = isErased ? memberRepository.findAllByErasedTrue() : memberRepository.findAllByErasedFalse();
        Stream<MemberEntity> stream;
        switch (searchType) {
            case 1 -> // telefon
                    stream = all.stream().filter(f -> f.getPhoneNumber().contains(inputText));
            case 2 -> // licencja
                    stream = all.stream().filter(f -> f.getLicense() != null && f.getLicense().getNumber() != null).filter(f -> f.getLicense().getNumber().contains(inputText));
            case 3 -> // email
                    stream = all.stream().filter(f -> f.getEmail() != null && f.getEmail().toLowerCase().contains(inputText.toLowerCase()));
            case 4 -> // PESEL
                    stream = all.stream().filter(f -> f.getPesel().contains(inputText));
            case 5 -> // dokument
                    stream = all.stream().filter(f -> f.getIDCard() != null && f.getIDCard().toLowerCase().contains(inputText.toLowerCase()));
            default -> stream = all.stream();
        }
        return stream.map(Mapping::map2DTO).sorted(Comparator.comparing(MemberDTO::getSecondName, PL_COLLATOR).thenComparing(MemberDTO::getFirstName, PL_COLLATOR)).toList();
    }

    public List<MemberInfo> getAllNamesErased() {
        return memberRepository.findAllByErasedTrue().stream().map(Mapping::map1).sorted(Comparator.comparing(MemberInfo::getSecondName, PL_COLLATOR).thenComparing(MemberInfo::getFirstName, PL_COLLATOR)).toList();
    }

    public List<MemberDTO> getAllMemberDTO() {
        return memberRepository.findAllByErasedFalse().stream().map(Mapping::map2DTO).sorted(Comparator.comparing(MemberDTO::getSecondName, PL_COLLATOR).thenComparing(MemberDTO::getFirstName, PL_COLLATOR)).toList();
    }

    public List<MemberDTO> getAllMemberDTO(Boolean adult, Boolean active, Boolean erase) {
        List<MemberEntity> source = erase ? memberRepository.findAllByErasedTrue() : memberRepository.findAllByErasedFalse();
        return source.stream().filter(m -> adult == null || Objects.equals(m.isAdult(), adult)).filter(m -> active == null || Objects.equals(m.isActive(), active)).map(Mapping::map2DTO).sorted(Comparator.comparing(MemberDTO::getSecondName, PL_COLLATOR).thenComparing(MemberDTO::getFirstName, PL_COLLATOR)).toList();
    }

    public List<String> getErasedType() {
        return Arrays.stream(ErasedType.values()).map(ErasedType::getName).toList();
    }

    public MemberEntity getMemberByUUID(String uuid) {
        return memberRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));
    }

    public Boolean getMemberPeselIsPresent(String pesel) {
        return memberRepository.findByPesel(pesel).isPresent();
    }

    public Boolean getMemberIDCardPresent(String idCard) {
        boolean present = memberRepository.findByIDCard(idCard).isPresent();
        LOG.info(present ? "Znaleziono osobę w bazie" : "Brak takiego numer w bazie");
        return present;
    }

    public Boolean getMemberEmailPresent(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    public ResponseEntity<?> findMemberByBarCode(String barcode) {
        return memberRepository.findByClubCardBarCode(barcode).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().body("Nie znaleziono Klubowicza"));
    }

    public List<Member> getMembersToReportToThePolice() {
        LocalDate notValidLicense = LocalDate.now().minusMonths(6);
        return memberRepository.findAllByErasedFalse().stream().filter(f -> f.getLicense() != null && f.getLicense().getNumber() != null).filter(f -> f.getClub() != null && f.getClub().getId() == 1).filter(f -> !f.getLicense().isValid()).filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense)).sorted(Comparator.comparing(MemberEntity::getSecondName, PL_COLLATOR)).map(Mapping::map).toList();
    }

    public List<Member> getMembersToErase() {
        LocalDate notValidContribution = LocalDate.now().minusMonths(6);

        return memberRepository.findAllByErasedFalseAndActiveFalse().stream().filter(f -> f.getHistory() != null && f.getHistory().getContributionList() != null && (f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().getFirst().getValidThru().minusDays(1).isBefore(notValidContribution))).sorted(Comparator.comparing(MemberEntity::getSecondName, PL_COLLATOR)).map(Mapping::map).toList();
    }

    public List<Member> getMembersErased(LocalDate firstDate, LocalDate secondDate) {
        LocalDate from = firstDate.minusDays(1);
        LocalDate to = secondDate.plusDays(1);

        return memberRepository.findAllByErasedTrue().stream().filter(f -> f.getErasedEntity() != null).filter(f -> f.getLicense() != null && !f.getLicense().isValid()).filter(f -> f.getErasedEntity().getDate().isAfter(from)).filter(f -> f.getErasedEntity().getDate().isBefore(to)).sorted(Comparator.comparing(MemberEntity::getSecondName, PL_COLLATOR)).map(Mapping::map).toList();
    }

    public List<Member> getMembersToReportToPoliceView(LocalDate firstDate, LocalDate secondDate) {
        LocalDate from = firstDate.minusDays(1);
        LocalDate to = secondDate.plusDays(1);

        return memberRepository.findAllByErasedTrue().stream().filter(f -> f.getErasedEntity() != null).filter(f -> f.getErasedEntity().getDate().isAfter(from)).filter(f -> f.getErasedEntity().getDate().isBefore(to)).sorted(Comparator.comparing(MemberEntity::getSecondName, PL_COLLATOR)).map(Mapping::map).toList();
    }

    public ResponseEntity<?> getMemberByPESELNumber(String peselNumber) {
        String normalized = peselNumber.replaceAll(" ", "");
        MemberEntity member = memberRepository.findAllByErasedFalse().stream().filter(f -> Objects.equals(f.getPesel(), normalized)).findFirst().orElse(null);
        return member != null ? ResponseEntity.ok(Mapping.map(member)) : ResponseEntity.badRequest().body("Brak numeru w Bazie");
    }

    @Transactional
    public ResponseEntity<?> changeClub(String uuid, int clubID) {
        MemberEntity member = memberRepository.findById(uuid).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono klubowicza");
        }
        ClubEntity club = clubRepository.findById(clubID).orElse(null);
        if (club == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubu");
        }
        member.setClub(club);
        memberRepository.save(member);
        return ResponseEntity.ok("Zmieniono Klub macierzysty zawodnika " + member.getFullName() + " na: " + club.getShortName());
    }

    @Transactional
    public ResponseEntity<?> toggleDeclaration(String uuid, boolean isSigned) {
        MemberEntity member = memberRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));
        boolean signed = member.toggleDeclaration(isSigned);
        boolean sex = member.getSex();
        memberRepository.save(member);
        return ResponseEntity.ok("Oznaczono, że " + member.getFullName() + " " + (signed ? "" : "nie ") + "podpisał" + (sex ? "a" : "") + " Deklaracji" + (signed ? "ę" : "i") + " LOK");
    }

    @Transactional
    public ResponseEntity<?> togglePzss(String uuid, boolean isSignedTo) {
        MemberEntity member = memberRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));
        boolean signedTo = member.togglePzss(isSignedTo);
        boolean sex = member.getSex();
        memberRepository.save(member);
        return ResponseEntity.ok("Oznaczono, że " + member.getFullName() + " " + (signedTo ? "" : "nie ") + "jest wpisani" + (sex ? "a" : "y") + " do portalu PZSS");
    }

    @Transactional
    public ResponseEntity<?> assignMemberToGroup(String memberUUID, Long groupId, String pinCode) throws NoUserPermissionException {
        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        MemberGroupEntity group = memberGroupRepository.findById(groupId).orElse(null);
        if (group == null || (group.getActive() != null && !group.getActive())) {
            LOG.info("Nie znaleziono grupy lub jest nieaktywna");
            return ResponseEntity.badRequest().body("Nie znaleziono grupy");
        }

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, member, HttpStatus.OK, "assignMemberToGroup", "Zaktualizowano dane klubowicza " + member.getFullName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            member.setMemberEntityGroup(group);
            memberRepository.save(member);
        }

        LOG.info("Przypisano Klubowicza " + member.getFullName() + " do grupy " + group.getName());
        return ResponseEntity.ok("Przypisano Klubowicza do grupy " + group.getName());
    }

    @Transactional
    public ResponseEntity<?> addNote(String uuid, String note) {
        note = note.equals("null") ? null : note;
        MemberEntity member = memberRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));
        member.setNote(note);
        String msg = note == null ? "Usunięto notatkę" : "Dodano notatkę";
        return ResponseEntity.ok(msg);
    }

    private String normalizeFirstName(String input) {
        return Arrays.stream(input.trim().split("\\s+")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }

    private String normalizePhone(String phone) {
        return "+48" + phone.replaceAll("\\s+", "");
    }
}
