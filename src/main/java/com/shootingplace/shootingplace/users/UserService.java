package com.shootingplace.shootingplace.users;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
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
import java.util.*;
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
    public ResponseEntity<?> createUser(String firstName, String secondName, List<String> userPermissionsList, String pinCode, String superPinCode, String memberUUID, Integer otherID) {
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
        boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().contentEquals(trim) && a.getSecondName().contentEquals(trim1));
        if (anyMatch) {
            return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
        }
        boolean b = userRepository.existsByPinCode(pin);
        if (b) {
            return ResponseEntity.badRequest().body("Wymyśl inny Kod PIN");
        }
        if (memberUUID != null && !memberUUID.isEmpty() && !memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza o podanym identyfikatorze - nie można utworzyć użytkownika");
        }
        try {
            userPinPolicy.validate(pinCode);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
        if (otherID == null) {
            otherID = 0;
        }
        UserEntity userEntity = UserEntity.builder().firstName(trim.toString()).secondName(trim1.toString()).pinCode(pin).active(true).otherID(otherID).member(memberRepository.findById(memberUUID).orElse(null)).build();
        userEntity.setUserPermissionsList(userPermissionsList);
        userRepository.save(userEntity);
        UserEntity actor = userAuthService.getAuthenticatedUser(superPinCode);
        changeHistoryService.record(actor, UserEntity.class.getSimpleName() + " createUser", userEntity.getUuid());
        return ResponseEntity.status(201).body("Utworzono użytkownika " + userEntity.getFullName());

    }

    public ResponseEntity<?> editUser(String firstName, String secondName, List<String> userPermissionsList, String pinCode, String superPinCode, String memberUUID, String otherID, String userUUID) {

        String superPin = Hashing.sha256().hashString(superPinCode, StandardCharsets.UTF_8).toString();
        if (userRepository.findAll().stream().filter(f -> f.getPinCode().equals(superPin)).noneMatch(a -> a.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName()))) {
            return ResponseEntity.badRequest().body("Nie można utworzyć użytkownika. Brak użytkownika z uprawnieniami.");
        }
        UserEntity entity = userRepository.findById(userUUID).orElseThrow(EntityNotFoundException::new);
        StringBuilder trim = new StringBuilder();
        StringBuilder trim1 = new StringBuilder();
        if (firstName != null && !firstName.equals("null") && !firstName.isEmpty()) {

            String[] s1 = firstName.split(" ");
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim.append(splinted);
            }
        } else {
            trim.append(entity.getFirstName());
        }
        if (secondName != null && !secondName.equals("null") && !secondName.isEmpty()) {

            String[] s2 = secondName.split(" ");
            for (String value : s2) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim1.append(splinted);
            }
        } else {
            trim1.append(entity.getSecondName());
        }
        if (firstName != null || secondName != null) {
            boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().contentEquals(trim) && a.getSecondName().contentEquals(trim1) && !a.getUuid().equals(userUUID));
            if (anyMatch) {
                return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
            } else {
                entity.setFirstName(trim.toString());
                entity.setSecondName(trim1.toString());
            }
        }
        if (pinCode != null) {
            try {
                userPinPolicy.validate(pinCode);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(409).body(e.getMessage());
            }

            String pin = Hashing.sha256()
                    .hashString(pinCode, StandardCharsets.UTF_8)
                    .toString();

            if (userRepository.existsByPinCode(pin)) {
                return ResponseEntity.badRequest().body("Taki kod już istnieje");
            }

            entity.setPinCode(pin);
        }
        if (userPermissionsList != null && !userPermissionsList.isEmpty()) {
            entity.setUserPermissionsList(userPermissionsList);
        }
        if (memberUUID != null && !memberUUID.equals("null") && !memberUUID.isEmpty() && !memberUUID.equals("undefined")) {
            if (memberRepository.existsById(memberUUID)) {
                entity.setMember(memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new));
                entity.setOtherID(null);
            }
        }
        if (otherID != null && !otherID.equals("null") && !otherID.isEmpty() && !otherID.equals("undefined")) {
            if (otherPersonRepository.existsById(Integer.parseInt(otherID))) {
                entity.setOtherID(Integer.valueOf(otherID));
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
        if (user.getMember() != null && user.getMember().getMemberPermissions() != null && user.getMember().getMemberPermissions().getArbiterNumber() != null) {
            return ResponseEntity.ok(user.getMember().getMemberPermissions().getArbiterNumber());
        }
        if (user.getOtherID() != null) {
            return otherPersonRepository.findById(user.getOtherID()).filter(op -> op.getPermissionsEntity() != null).filter(op -> op.getPermissionsEntity().getArbiterNumber() != null).map(op -> tournamentRepository.findByOpenIsTrue().getUuid()).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().body("użytkownik nie posiada licencji sędziowskiej"));
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

    public ResponseEntity<?> deleteUser(String userID, String code) throws NoUserPermissionException {
        String pin = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();
        UserEntity targetUser = userRepository.findById(userID).orElseThrow(NoUserPermissionException::new);
        UserEntity authUser = userRepository.findByPinCode(pin).orElse(null);
        if (authUser == null) {
            throw new NoUserPermissionException();
        }
        if (authUser.getUserPermissionsList() == null || !authUser.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName())) {
            throw new NoUserPermissionException();
        }
        targetUser.setActive(false);
        userRepository.save(targetUser);
        LOG.info("Zmiana statusu na nieaktywny: {}", targetUser.getFullName());
        return ResponseEntity.ok("usunięto użytkownika " + targetUser.getFullName());
    }


    public String permissionsByPin(String pinCode) {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin).orElseThrow(EntityNotFoundException::new);

        Map<String, String> map = new HashMap<>();

        map.put("Permissions", userEntity.getUserPermissionsList().toString());
        map.put("Hash", pin);
        return String.valueOf(map);
    }

}
