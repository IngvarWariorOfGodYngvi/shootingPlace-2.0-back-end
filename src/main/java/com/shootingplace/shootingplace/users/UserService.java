package com.shootingplace.shootingplace.users;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.license.LicenseRepository;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import com.shootingplace.shootingplace.tournament.TournamentService;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceEntity;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ChangeHistoryService changeHistoryService;
    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final ContributionRepository contributionRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final TournamentService tournamentService;
    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;

    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public UserService(UserRepository userRepository, ChangeHistoryService changeHistoryService, MemberRepository memberRepository, LicenseRepository licenseRepository, ContributionRepository contributionRepository, OtherPersonRepository otherPersonRepository, TournamentService tournamentService, TournamentRepository tournamentRepository, ClubRepository clubRepository, WorkingTimeEvidenceRepository workingTimeEvidenceRepository) {
        this.userRepository = userRepository;
        this.changeHistoryService = changeHistoryService;
        this.memberRepository = memberRepository;
        this.licenseRepository = licenseRepository;
        this.contributionRepository = contributionRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.tournamentService = tournamentService;
        this.tournamentRepository = tournamentRepository;
        this.clubRepository = clubRepository;
        this.workingTimeEvidenceRepository = workingTimeEvidenceRepository;
    }

    public List<UserDTO> getListOfUser() {
        return userRepository.findAll()
                .stream()
                .filter(UserEntity::isActive)
                .filter(f -> !f.getUserPermissionsList().contains(UserSubType.ADMIN.getName()))
                .map(Mapping::map)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> createUser(String firstName, String secondName, List<String> userPermissionsList, String pinCode, String superPinCode, String memberUUID, Integer otherID) throws NoUserPermissionException {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        String superPin = Hashing.sha256().hashString(superPinCode, StandardCharsets.UTF_8).toString();
        if (userRepository.findAll().stream().filter(f -> f.getPinCode().equals(superPin)).noneMatch(a -> a.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName()))) {
            return ResponseEntity.badRequest().body("Nie można utworzyć użytkownika. Brak użytkownika z uprawnieniami.");
        }
        if ((firstName.trim().isEmpty() || firstName.equals("null")) || secondName.trim().isEmpty() || secondName.equals("null") || (pinCode.trim().isEmpty() || pinCode.equals("null"))) {
            return ResponseEntity.badRequest().body("Musisz podać jakieś informacje.");
        }
        String[] s1 = firstName.split(" ");
        StringBuilder trim = new StringBuilder();
        for (String value : s1) {
            String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
            trim.append(splinted);
        }
        String[] s2 = secondName.split(" ");
        StringBuilder trim1 = new StringBuilder();
        for (String value : s2) {
            String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
            trim1.append(splinted);
        }
        boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().equals(trim.toString()) && a.getSecondName().equals(trim1.toString()));
        if (anyMatch) {
            return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
        }
        boolean b = userRepository.existsByPinCode(pin);
        if (b) {
            return ResponseEntity.badRequest().body("Wymyśl inny Kod PIN");
        }
        if (memberUUID != null && !memberUUID.equals("") && !memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza o podanym identyfikatorze - nie można utworzyć użytkownika");
        }

        String trim2 = pinCode.trim();
        char[] pinNumbers = trim2.toCharArray();
        if (pinNumbers.length < 4) {
            ResponseEntity.status(409).body("Kod jest za krótki. Musi posiadać 4 cyfry.");
        }
        int p1 = Integer.parseInt(String.valueOf(pinNumbers[0]));
        int p2 = Integer.parseInt(String.valueOf(pinNumbers[1]));
        int p3 = Integer.parseInt(String.valueOf(pinNumbers[2]));
        int p4 = Integer.parseInt(String.valueOf(pinNumbers[3]));
        boolean c = p1 == p2 && p2 == p3 && p3 == p4;
        for (int i = 0; i < pinNumbers.length; i++) {
            if (c) {
                LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
            }
        }
        if (p4 == 0) {
            p4 = 10;
        }
        if (p1 + 1 == p2 && p2 + 1 == p3 && p3 + 1 == p4) {
            LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
            return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
        }
        if (p4 == 10) {
            p4 = 0;
        }
        if (p1 == 0) {
            p1 = 10;
        }
        if (p1 - 1 == p2 && p2 - 1 == p3 && p3 - 1 == p4) {
            LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
            return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
        }
        if (otherID == null) {
            otherID = 0;
        }
        UserEntity userEntity = UserEntity.builder()
                .firstName(trim.toString())
                .secondName(trim1.toString())
                .pinCode(pin)
                .active(true)
                .otherID(otherID)
                .member(memberRepository.existsById(memberUUID) ? memberRepository.getOne(memberUUID) : null)
                .build();
        userEntity.setUserPermissionsList(userPermissionsList);
        userRepository.save(userEntity);
        changeHistoryService.addRecordToChangeHistory(superPinCode, userEntity.getClass().getSimpleName() + " " + "createUser", userEntity.getUuid());
        return ResponseEntity.status(201).body("Utworzono użytkownika " + userEntity.getFullName());

    }

    public ResponseEntity<?> editUser(String firstName, String secondName, List<String> userPermissionsList, String pinCode, String superPinCode, String memberUUID, String otherID, String userUUID) {

        String superPin = Hashing.sha256().hashString(superPinCode, StandardCharsets.UTF_8).toString();
        if (userRepository.findAll().stream().filter(f -> f.getPinCode().equals(superPin)).noneMatch(a -> a.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName()))) {
            return ResponseEntity.badRequest().body("Nie można utworzyć użytkownika. Brak użytkownika z uprawnieniami.");
        }
        UserEntity entity = userRepository.getOne(userUUID);
        StringBuilder trim = new StringBuilder();
        StringBuilder trim1 = new StringBuilder();
        if (firstName != null && !firstName.equals("null") && !firstName.equals("")) {

            String[] s1 = firstName.split(" ");
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim.append(splinted);
            }
        } else {
            trim.append(entity.getFirstName());
        }
        if (secondName != null && !secondName.equals("null") && !secondName.equals("")) {

            String[] s2 = secondName.split(" ");
            for (String value : s2) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim1.append(splinted);
            }
        } else {
            trim1.append(entity.getSecondName());
        }
        if (firstName != null || secondName != null) {
            boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().equals(trim.toString()) && a.getSecondName().equals(trim1.toString()) && !a.getUuid().equals(userUUID));
            if (anyMatch) {
                return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
            } else {
                entity.setFirstName(trim.toString());
                entity.setSecondName(trim1.toString());
            }
        }
        if (pinCode != null) {
            String trim2 = pinCode.trim();
            String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
            if (userRepository.existsByPinCode(pin)) {
                return ResponseEntity.badRequest().body("Taki kod już istnieje. Wymyśl coś innego");
            } else {

                char[] pinNumbers = trim2.toCharArray();
                if (pinNumbers.length < 4) {
                    ResponseEntity.status(409).body("Kod jest za krótki. Musi posiadać 4 cyfry.");
                }
                int p1 = Integer.parseInt(String.valueOf(pinNumbers[0]));
                int p2 = Integer.parseInt(String.valueOf(pinNumbers[1]));
                int p3 = Integer.parseInt(String.valueOf(pinNumbers[2]));
                int p4 = Integer.parseInt(String.valueOf(pinNumbers[3]));
                boolean b = p1 == p2 && p2 == p3 && p3 == p4;
                for (int i = 0; i < pinNumbers.length; i++) {
                    if (b) {
                        LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                        return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                    }
                }
                if (p4 == 0) {
                    p4 = 10;
                }
                if (p1 + 1 == p2 && p2 + 1 == p3 && p3 + 1 == p4) {
                    LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                    return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                }
                if (p4 == 10) {
                    p4 = 0;
                }
                if (p1 == 0) {
                    p1 = 10;
                }
                if (p1 - 1 == p2 && p2 - 1 == p3 && p3 - 1 == p4) {
                    LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                    return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                }
                entity.setPinCode(pin);
            }
        }
        if (userPermissionsList != null && !userPermissionsList.isEmpty()) {
            entity.setUserPermissionsList(userPermissionsList);
        }
        if (memberUUID != null && !memberUUID.equals("null") && !memberUUID.equals("") && !memberUUID.equals("undefined")) {
            if (memberRepository.existsById(memberUUID)) {
                entity.setMember(memberRepository.getOne(memberUUID));
                entity.setOtherID(null);
            }
        }
        if (otherID != null && !otherID.equals("null") && !otherID.equals("") && !otherID.equals("undefined")) {
            if (otherPersonRepository.existsById(Integer.parseInt(otherID))) {
                entity.setOtherID(Integer.valueOf(otherID));
                entity.setMember(null);
            }
        }
        userRepository.save(entity);
        return ResponseEntity.ok("aktualizowano użytkownika");
    }

    public ResponseEntity<?> getUserActions(String uuid) {
        UserEntity one = userRepository.getOne(uuid);

        List<ChangeHistoryDTO> all = one.getList()
                .stream()
                .filter(f -> !f.getUserEntity().getFirstName().equals("Admin"))
                .map(Mapping::map)
                .sorted(Comparator.comparing(ChangeHistoryDTO::getDayNow).thenComparing(ChangeHistoryDTO::getTimeNow).reversed())
                .collect(Collectors.toList());
//        all.forEach(e -> {
//                    if (e.getBelongsTo() != null) {
//                        if (memberRepository.existsById(e.getBelongsTo())) {
//                            MemberEntity member = memberRepository.findById(e.getBelongsTo()).orElseThrow(EntityNotFoundException::new);
//                            e.setBelongsTo(member.getSecondName().concat(" " + member.getFirstName()));
//                        }
//                        if (contributionRepository.existsById(e.getBelongsTo())) {
//                            MemberEntity member = memberRepository.findByHistoryUuid(contributionRepository.getOne(e.getBelongsTo()).getHistoryUUID());
//                            e.setBelongsTo(member.getSecondName().concat(" " + member.getFirstName()));
//                        }
//                        if (licenseRepository.existsById(e.getBelongsTo())) {
//                            MemberEntity member = memberRepository.findByLicenseUuid(e.getBelongsTo());
//                            e.setBelongsTo(member.getSecondName().concat(" " + member.getFirstName()));
//
//                        }
//                    } else {
//                        e.setBelongsTo("operacja");
//                    }
//                }
//        );
        return ResponseEntity.ok(all);

    }


    public ResponseEntity<?> getAccess(String pinCode) throws NoUserPermissionException {
        ResponseEntity<?> response;
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        if (userRepository.existsByPinCode(pin)) {
            UserEntity user = userRepository.findByPinCode(pin);

            WorkingTimeEvidenceEntity workingTimeEvidenceEntity = workingTimeEvidenceRepository.findAll()
                    .stream()
                    .filter(f -> f.getUser().getPinCode().equals(pin))
                    .filter(f -> !f.isClose())
                    .findFirst()
                    .orElse(null);
            if ((workingTimeEvidenceEntity != null && user.getUserPermissionsList().contains(UserSubType.MANAGEMENT.getName())) || user.getUserPermissionsList().contains(UserSubType.ADMIN.getName()) || user.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName())) {
                response = ResponseEntity.ok().build();
                changeHistoryService.addRecordToChangeHistory(pin, "uzyskaj dostęp", user.getMember() != null ? user.getMember().getUuid() : null);
            } else {
                response = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Brak dostępu ");
            }

        } else {
            response = ResponseEntity.badRequest().body("Błędny kod");
        }


        return response;
    }

    public ResponseEntity<?> checkArbiterByCode(String code) {
        String pin = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.existsByPinCode(pin) ? userRepository.findByPinCode(pin) : null;
        if (user != null) {
            if (tournamentService.checkAnyOpenTournament()) {
                if (user.getMember() != null) {
                    if (user.getMember().getMemberPermissions().getArbiterNumber() != null) {
                        return ResponseEntity.ok(user.getMember().getMemberPermissions().getArbiterNumber());
                    } else {
                        return ResponseEntity.badRequest().body("użytkownik nie posiada licencji sędziowskiej");
                    }
                } else {
                    if (otherPersonRepository.getOne(user.getOtherID()).getPermissionsEntity().getArbiterNumber() != null) {

                        return ResponseEntity.ok(tournamentRepository.findByOpenIsTrue().getUuid());
                    } else {
                        return ResponseEntity.badRequest().body("użytkownik nie posiada licencji sędziowskiej");
                    }
                }
            } else {
                return ResponseEntity.badRequest().body("Żadne zawody nie są otwarte");
            }
        }

        return ResponseEntity.badRequest().body("Próba się nie powiodła");
    }

    // Minimalne wymagania aby zwróciło false:
    // minimum 1 Klub
    // minimum 1 SuperUser
    // minimum 1 User
    // nie wolno brać pod uwagę Admina
    public ResponseEntity<?> checkFirstStart() {

        if (clubRepository.getOne(1).getShortName().equals("firstStart"))
            return ResponseEntity.ok(true);

        List<UserEntity> collect = userRepository.findAll()
                .stream()
                .filter(f -> !f.getSecondName().equals("Admin")).toList();

        long superUserCount = collect.stream().filter(f -> f.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName())).count();
        long userCount = collect.stream().filter(f -> !f.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName())).count();
        if (superUserCount == 0 && userCount == 0) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }

    public List<String> getPermissions() {
        return Arrays.stream(UserSubType.values())
                .map(UserSubType::getName)
                .filter(name -> !name.equals(UserSubType.ADMIN.getName())).collect(Collectors.toList());
    }

    public ResponseEntity<?> deleteUser(String userID, String code) throws NoUserPermissionException {
        String pin = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();
        UserEntity one = userRepository.getOne(userID);
        UserEntity user = userRepository.existsByPinCode(pin) ? userRepository.findByPinCode(pin) : null;
        if (one == null || user == null || !user.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName())) {
            throw new NoUserPermissionException();
        }
        one.setActive(false);
        userRepository.save(one);
        LOG.info("Zmiana statusu na nieaktywny " + one.getFullName());
        return ResponseEntity.ok("usunięto użytkownika " + one.getFullName());
    }

    public String permissionsByPin(String pinCode) {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin);

        Map<String, String> map = new HashMap<>();

        map.put("Permissions", userEntity.getUserPermissionsList().toString());
        map.put("Hash", pin);
        return String.valueOf(map);
    }

}
