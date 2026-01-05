package com.shootingplace.shootingplace.users;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.changeHistory.ChangeHistoryDTO;
import com.shootingplace.shootingplace.history.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.history.changeHistory.ChangeHistoryService;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.security.UserAuthService;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import com.shootingplace.shootingplace.tournament.TournamentService;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ChangeHistoryService changeHistoryService;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final TournamentService tournamentService;
    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final UserAuthService userAuthService;
    private final UserPinPolicy userPinPolicy;
    private final Logger LOG = LogManager.getLogger(getClass());

    public List<UserDTO> getListOfUser() {
        return userRepository.findAll().stream().filter(UserEntity::isActive).filter(f -> !f.getUserPermissionsList().contains(UserSubType.ADMIN.getName())).map(Mapping::map).collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<?> createUser(UserCreateDTO user, String superPinCode) {
        String pin = Hashing.sha256().hashString(user.getPinCode(), StandardCharsets.UTF_8).toString();
        String superPin = Hashing.sha256().hashString(superPinCode, StandardCharsets.UTF_8).toString();
        if (userRepository.findAll().stream().filter(f -> f.getPinCode().equals(superPin)).noneMatch(a -> a.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName()))) {
            return ResponseEntity.badRequest().body("Nie można utworzyć użytkownika. Brak użytkownika z uprawnieniami.");
        }
        if ((user.getFirstName().isEmpty() || user.getFirstName().equals("null")) || user.getSecondName().trim().isEmpty() || user.getSecondName().equals("null") || (user.getPinCode().trim().isEmpty() || user.getPinCode().equals("null"))) {
            return ResponseEntity.badRequest().body("Musisz podać jakieś informacje.");
        }
        String[] s1 = user.getFirstName().split(" ");
        StringBuilder trim = new StringBuilder();
        for (String value : s1) {
            String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
            trim.append(splinted);
        }
        String[] s2 = user.getSecondName().split(" ");
        StringBuilder trim1 = new StringBuilder();
        for (String value : s2) {
            String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
            trim1.append(splinted);
        }
        boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().contentEquals(trim) && a.getSecondName().contentEquals(trim1));
        if (anyMatch) {
            return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
        }
        boolean b = userRepository.existsByPinCode(pin);
        if (b) {
            return ResponseEntity.badRequest().body("Wymyśl inny Kod PIN");
        }
        if (user.getMemberUUID() != null && !user.getMemberUUID().isEmpty() && !memberRepository.existsById(user.getMemberUUID())) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza o podanym identyfikatorze - nie można utworzyć użytkownika");
        }
        try {
            userPinPolicy.validate(user.getPinCode());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
        UserEntity userEntity = UserEntity.builder()
                .firstName(trim.toString())
                .secondName(trim1.toString())
                .pinCode(pin)
                .active(true)
                .otherID(user.getOtherID())
                .member(memberRepository.findById(user.getMemberUUID()).orElse(null))
                .build();
        userEntity.setUserPermissionsList(user.getUserPermissionsList());
        userRepository.save(userEntity);
        UserEntity actor = userAuthService.getAuthenticatedUser(superPinCode);
        changeHistoryService.record(actor, UserEntity.class.getSimpleName() + " createUser", userEntity.getUuid());
        return ResponseEntity.status(201).body("Utworzono użytkownika " + userEntity.getFullName());

    }

    public ResponseEntity<?> editUser(UserCreateDTO user, String superPinCode, String userUUID) {
        System.out.println(userUUID);
        String superPin = Hashing.sha256().hashString(superPinCode, StandardCharsets.UTF_8).toString();
        if (userRepository.findAll().stream().filter(f -> f.getPinCode().equals(superPin)).noneMatch(a -> a.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName()))) {
            return ResponseEntity.badRequest().body("Nie można utworzyć użytkownika. Brak użytkownika z uprawnieniami.");
        }
        UserEntity entity = userRepository.findById(userUUID).orElseThrow(EntityNotFoundException::new);
        StringBuilder trim = new StringBuilder();
        StringBuilder trim1 = new StringBuilder();
        if (user.getFirstName() != null && !user.getFirstName().equals("null") && !user.getFirstName().isEmpty()) {

            String[] s1 = user.getFirstName().split(" ");
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim.append(splinted);
            }
        } else {
            trim.append(entity.getFirstName());
        }
        if (user.getSecondName() != null && !user.getSecondName().equals("null") && !user.getSecondName().isEmpty()) {

            String[] s2 = user.getSecondName().split(" ");
            for (String value : s2) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim1.append(splinted);
            }
        } else {
            trim1.append(entity.getSecondName());
        }
        if (user.getFirstName() != null || user.getSecondName() != null) {
            boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().contentEquals(trim) && a.getSecondName().contentEquals(trim1) && !a.getUuid().equals(userUUID));
            if (anyMatch) {
                return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
            } else {
                entity.setFirstName(trim.toString());
                entity.setSecondName(trim1.toString());
            }
        }
        if (user.getPinCode() != null) {
            try {
                userPinPolicy.validate(user.getPinCode());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(409).body(e.getMessage());
            }

            String pin = Hashing.sha256()
                    .hashString(user.getPinCode(), StandardCharsets.UTF_8)
                    .toString();

            if (userRepository.existsByPinCode(pin)) {
                return ResponseEntity.badRequest().body("Taki kod już istnieje");
            }

            entity.setPinCode(pin);
        }
        if (user.getUserPermissionsList() != null && !user.getUserPermissionsList().isEmpty()) {
            entity.setUserPermissionsList(user.getUserPermissionsList());
        }
        if (user.getMemberUUID() != null && !user.getMemberUUID().equals("null") && !user.getMemberUUID().isEmpty() && !user.getMemberUUID().equals("undefined")) {
            if (memberRepository.existsById(user.getMemberUUID())) {
                entity.setMember(memberRepository.findById(user.getMemberUUID()).orElseThrow(EntityNotFoundException::new));
                entity.setOtherID(null);
            }
        }
        if (user.getOtherID() != null && user.getOtherID() != 0) {
            if (otherPersonRepository.existsById(user.getOtherID())) {
                entity.setOtherID(user.getOtherID());
                entity.setMember(null);
            }
        }
        userRepository.save(entity);
        return ResponseEntity.ok("aktualizowano użytkownika");
    }

    public ResponseEntity<?> getUserActions(String userUUID) {
        UserEntity one = userRepository.findById(userUUID).orElseThrow(EntityNotFoundException::new);

        List<ChangeHistoryDTO> all = one.getList().stream().filter(f -> !f.getUserEntity().getFirstName().equals("Admin")).map(Mapping::map).sorted(Comparator.comparing(ChangeHistoryDTO::getDayNow).thenComparing(ChangeHistoryDTO::getTimeNow).reversed()).collect(Collectors.toList());
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

    public ResponseEntity<?> checkArbiterByCode(String code) {
        String pin = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();
        Optional<UserEntity> userOpt = userRepository.findByPinCode(pin);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Próba się nie powiodła");
        }
        if (!tournamentService.checkAnyOpenTournament()) {
            return ResponseEntity.badRequest().body("Żadne zawody nie są otwarte");
        }
        UserEntity user = userOpt.get();
        if (user.getMember() != null && user.getMember().getMemberPermissions() != null && user.getMember().getMemberPermissions().getArbiterStaticNumber() != null) {
            return ResponseEntity.ok(user.getMember().getMemberPermissions().getArbiterStaticNumber());
        }
        if (user.getOtherID() != null) {
            return otherPersonRepository.findById(user.getOtherID()).filter(op -> op.getPermissionsEntity() != null).filter(op -> op.getPermissionsEntity().getArbiterStaticNumber() != null).map(op -> tournamentRepository.findByOpenIsTrue().getUuid()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().body("użytkownik nie posiada licencji sędziowskiej"));
        }
        return ResponseEntity.badRequest().body("użytkownik nie posiada licencji sędziowskiej");
    }

    // Minimalne wymagania, aby zwróciło false:
    // minimum 1 Klub
    // minimum 1 superuser
    // minimum 1 User
    // nie wolno brać pod uwagę Admina
    public ResponseEntity<?> checkFirstStart() {

        if (clubRepository.findById(1).orElseThrow(EntityNotFoundException::new).getShortName().equals("firstStart"))
            return ResponseEntity.ok(true);

        List<UserEntity> collect = userRepository.findAll().stream().filter(f -> !f.getSecondName().equals("Admin")).toList();

        long superUserCount = collect.stream().filter(f -> f.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName())).count();
        long userCount = collect.stream().filter(f -> !f.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName())).count();
        if (superUserCount == 0 && userCount == 0) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }

    public List<String> getPermissions() {
        return Arrays.stream(UserSubType.values()).map(UserSubType::getName).filter(name -> !name.equals(UserSubType.ADMIN.getName())).collect(Collectors.toList());
    }

    @RecordHistory(action = "User.delete", entity = HistoryEntityType.USER, entityArgIndex = 0)
    public ResponseEntity<?> deleteUser(String userID) {
        UserEntity targetUser = userRepository.findById(userID).orElseThrow(EntityNotFoundException::new);
        targetUser.setActive(false);
        userRepository.save(targetUser);
        LOG.info("Zmiana statusu na nieaktywny: {}", targetUser.getFullName());
        return ResponseEntity.ok("usunięto użytkownika " + targetUser.getFullName());
    }


}
