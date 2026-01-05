package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.contributions.ContributionService;
import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.enums.ErasedType;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.history.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.license.LicenseRepository;
import com.shootingplace.shootingplace.license.LicenseService;
import com.shootingplace.shootingplace.permissions.PermissionService;
import com.shootingplace.shootingplace.security.UserAuthContext;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentService;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private final PermissionService permissionService;
    private final ClubRepository clubRepository;
    private final ErasedRepository erasedRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final EmailService emailService;
    private final UserAuthContext userAuthContext;

    private static final Logger LOG = LogManager.getLogger(MemberService.class);
    private static final Collator PL_COLLATOR = Collator.getInstance(Locale.forLanguageTag("pl"));

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
    @RecordHistory(action = "Member.addNew", entity = HistoryEntityType.MEMBER)
    public ResponseEntity<?> addNewMember(Member member, Address address, boolean returningToClub) {

        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }

        List<MemberEntity> allMembers = memberRepository.findAll();

        MemberEntity byPesel = allMembers.stream().filter(m -> Objects.equals(m.getPesel(), member.getPesel())).findFirst().orElse(null);

        if (byPesel != null && !(returningToClub && byPesel.isErased())) {
            return ResponseEntity.badRequest().body("Uwaga! Ktoś już ma taki numer PESEL");
        }

        String emailInput = member.getEmail();
        boolean emailTaken = allMembers.stream().map(MemberEntity::getEmail).filter(Objects::nonNull).filter(s -> !s.isEmpty()).anyMatch(e -> e.equalsIgnoreCase(emailInput));

        if (emailTaken && !returningToClub) {
            return ResponseEntity.badRequest().body("Uwaga! Ktoś już ma taki e-mail");
        }

        if (member.getLegitimationNumber() != null && memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent() && !returningToClub) {

            return ResponseEntity.badRequest().body("Uwaga! Ktoś już ma taki numer legitymacji");
        }

        boolean duplicatedIdCard = allMembers.stream().filter(m -> !m.isErased()).map(MemberEntity::getIDCard).filter(Objects::nonNull).anyMatch(id -> id.trim().equalsIgnoreCase(member.getIDCard()));

        if (duplicatedIdCard && !returningToClub) {
            return ResponseEntity.badRequest().body("Uwaga! Ktoś już ma taki numer dowodu osobistego");
        }

        LocalDate joinDate = member.getJoinDate() != null ? member.getJoinDate() : LocalDate.now();

        int legitimationNumber = member.getLegitimationNumber() != null ? member.getLegitimationNumber() : allMembers.stream().map(MemberEntity::getLegitimationNumber).filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;

        member.setFirstName(normalizeFirstName(member.getFirstName()));
        member.setSecondName(member.getSecondName().toUpperCase());
        member.setPhoneNumber(normalizePhone(member.getPhoneNumber()));
        member.setEmail(member.getEmail() == null ? "" : member.getEmail().toLowerCase());
        member.setJoinDate(joinDate);
        member.setLegitimationNumber(legitimationNumber);
        member.setAdult(member.getAdult());
        member.setAddress(address);
        member.setIDCard(member.getIDCard().trim().toUpperCase());
        member.setPesel(member.getPesel());

        member.setClub(clubRepository.findById(1).orElseThrow());
        member.setShootingPatent(shootingPatentService.getShootingPatent());
        member.setLicense(licenseService.getLicense());
        member.setHistory(historyService.getHistory());
        member.setWeaponPermission(weaponPermissionService.getWeaponPermission());
        member.setMemberPermissions(permissionService.getMemberPermissions());
        member.setPersonalEvidence(PersonalEvidence.builder().ammoList(new ArrayList<>()).build());

        member.setPzss(false);
        member.setErased(false);
        member.setActive(true);

        MemberEntity saved = memberRepository.save(Mapping.map(member));
        MemberGroupEntity group = memberGroupRepository.findByName(member.getGroup()).orElseThrow(() -> new IllegalStateException("Nie znaleziono grupy"));

        saved.setSignBy(user.getFullName());
        saved.setMemberEntityGroup(group);
        memberRepository.save(saved);
        historyService.addContribution(saved.getUuid(), contributionService.addFirstContribution(LocalDate.now(), user));

        emailService.sendRegistrationConfirmation(saved.getUuid());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved.getUuid());
    }


    @Transactional
    @RecordHistory(action = "Member.toggleActive", entity = HistoryEntityType.MEMBER, entityArgIndex = 0)
    public ResponseEntity<?> activateOrDeactivateMember(String uuid) {

        MemberEntity member = memberRepository.findById(uuid).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        member.toggleActive();
        memberRepository.save(member);

        return ResponseEntity.ok("Zmieniono status aktywny/nieaktywny");
    }


    @Transactional
    @RecordHistory(action = "Member.changeAdult", entity = HistoryEntityType.MEMBER, entityArgIndex = 0)
    public ResponseEntity<?> changeAdult(String memberUUID) {

        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        if (member.isAdult()) {
            return ResponseEntity.badRequest().body("Klubowicz należy już do grupy dorosłej");
        }

        member.setAdult(true);
        memberRepository.save(member);

        return ResponseEntity.ok("Klubowicz należy od teraz do grupy dorosłej");
    }


    @Transactional
    @RecordHistory(action = "Member.erase", entity = HistoryEntityType.MEMBER, entityArgIndex = 0)
    public ResponseEntity<?> eraseMember(String memberUUID, String erasedType, LocalDate erasedDate, String additionalDescription) {

        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        if (member.isErased()) {
            return ResponseEntity.badRequest().body("Klubowicz jest już skreślony");
        }

        ErasedEntity erased = ErasedEntity.builder().erasedType(erasedType).date(erasedDate).additionalDescription(additionalDescription).inputDate(LocalDate.now()).build();

        erasedRepository.save(erased);

        member.setErasedEntity(erased);
        member.toggleErase();
        member.setPzss(false);

        memberRepository.save(member);

        return ResponseEntity.ok("Usunięto Klubowicza");
    }


    @Transactional
    @RecordHistory(action = "Member.update", entity = HistoryEntityType.MEMBER, entityArgIndex = 0)
    public ResponseEntity<?> updateMember(String memberUUID, Member member) {

        MemberEntity entity = memberRepository.findById(memberUUID).orElse(null);
        if (entity == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        // imię
        if (member.getFirstName() != null && !member.getFirstName().isEmpty()) {
            entity.setFirstName(normalizeFirstName(member.getFirstName()));
            LOG.info("Zaktualizowano Imię");
        }

        // nazwisko
        if (member.getSecondName() != null && !member.getSecondName().isEmpty()) {
            entity.setSecondName(member.getSecondName().toUpperCase());
            LOG.info("Zaktualizowano Nazwisko");
        }

        // data przystąpienia
        if (member.getJoinDate() != null) {
            entity.setJoinDate(member.getJoinDate());
            LOG.info("Zaktualizowano Datę przystąpienia");
        }

        // legitymacja
        if (member.getLegitimationNumber() != null) {
            boolean exists = memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();

            if (exists) {
                LOG.warn("Już ktoś ma taki numer legitymacji");
                return ResponseEntity.badRequest().body("Uwaga! Już ktoś ma taki numer legitymacji");
            }

            entity.setLegitimationNumber(member.getLegitimationNumber());
            LOG.info("Zaktualizowano Numer Legitymacji");
        }

        // email
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            String email = member.getEmail().trim().toLowerCase();

            boolean emailExists = memberRepository.findByEmail(email).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();

            if (emailExists) {
                LOG.error("Już ktoś ma taki sam e-mail");
                return ResponseEntity.badRequest().body("Uwaga! Już ktoś ma taki sam e-mail");
            }

            entity.setEmail(email);
            LOG.info("Zaktualizowano Email");
        }

        // telefon
        if (member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty()) {
            String normalized = normalizePhone(member.getPhoneNumber());

            if (normalized.length() != 9) {
                LOG.error("Źle podany numer telefonu");
                return ResponseEntity.badRequest().body("Nieprawidłowy numer telefonu");
            }

            boolean phoneExists = memberRepository.findByPhoneNumber(normalized).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();

            if (phoneExists) {
                LOG.error("Ktoś już ma taki numer telefonu");
                return ResponseEntity.badRequest().body("Ktoś już ma taki numer telefonu");
            }

            entity.setPhoneNumber(normalized);
            LOG.info("Zaktualizowano Numer Telefonu");
        }

        // dowód
        if (member.getIDCard() != null && !member.getIDCard().isEmpty()) {
            String id = member.getIDCard().trim().toUpperCase();

            boolean idExists = memberRepository.findByIDCard(id).filter(m -> !m.getUuid().equals(entity.getUuid())).isPresent();

            if (idExists) {
                LOG.error("Ktoś już ma taki numer dowodu");
                return ResponseEntity.badRequest().body("Ktoś już ma taki numer dowodu");
            }

            entity.setIDCard(id);
            LOG.info("Zaktualizowano Numer Dowodu");
        }

        memberRepository.save(entity);

        return ResponseEntity.ok("Zaktualizowano dane klubowicza " + entity.getFullName());
    }


    public ResponseEntity<?> getMember(int number) {
        if (!memberRepository.existsByLegitimationNumber(number)) {
            return ResponseEntity.badRequest().body("Klubowicz o podanym numerze legitymacji nie istnieje");
        }
        MemberEntity member = memberRepository.findByLegitimationNumber(number).orElseThrow(EntityNotFoundException::new);
        historyService.checkStarts(member.getUuid());
        LOG.info("Wywołano Klubowicza {}", member.getFullName());
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
    @RecordHistory(action = "Member.assignToGroup", entity = HistoryEntityType.MEMBER, entityArgIndex = 0)
    public ResponseEntity<?> assignMemberToGroup(String memberUUID, Long groupId) {

        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        MemberGroupEntity group = memberGroupRepository.findById(groupId).orElse(null);
        if (group == null || Boolean.FALSE.equals(group.getActive())) {
            LOG.info("Nie znaleziono grupy lub jest nieaktywna");
            return ResponseEntity.badRequest().body("Nie znaleziono grupy lub grupa jest nieaktywna");
        }

        member.setMemberEntityGroup(group);
        memberRepository.save(member);

        LOG.info("Przypisano Klubowicza {} do grupy {}", member.getFullName(), group.getName());

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
