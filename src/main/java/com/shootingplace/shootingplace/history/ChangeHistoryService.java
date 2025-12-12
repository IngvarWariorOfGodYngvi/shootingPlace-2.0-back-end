package com.shootingplace.shootingplace.history;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;
    private final UserRepository userRepository;
    private final WorkingTimeEvidenceService workServ;


    public ChangeHistoryService(ChangeHistoryRepository changeHistoryRepository, UserRepository userRepository, WorkingTimeEvidenceService workServ) {
        this.changeHistoryRepository = changeHistoryRepository;
        this.userRepository = userRepository;
        this.workServ = workServ;
    }


    private ChangeHistoryEntity addRecord(UserEntity user, String classNamePlusMethod, String uuid) {
        return changeHistoryRepository.save(ChangeHistoryEntity.builder()
                .userEntity(user)
                .classNamePlusMethod(classNamePlusMethod)
                .belongsTo(uuid)
                .dayNow(LocalDate.now())
                .timeNow(String.valueOf(LocalTime.now()))
                .build());
    }

    public ResponseEntity<?> comparePinCode(String pinCode, List<String> acceptedPermissions)
            throws NoUserPermissionException {
        String pin = Hashing.sha256()
                .hashString(pinCode, StandardCharsets.UTF_8)
                .toString();
        UserEntity user = userRepository.findByPinCode(pin).orElse(null);
        if (user == null) {
            throw new NoUserPermissionException();
        }
        List<String> userPermissions = user.getUserPermissionsList();
        if (userPermissions == null) {
            throw new NoUserPermissionException();
        }
        boolean hasAnyRequiredPermission = userPermissions.stream()
                .anyMatch(acceptedPermissions::contains);
        if (!hasAnyRequiredPermission) {
            throw new NoUserPermissionException();
        }
        if (acceptedPermissions.contains(UserSubType.ADMIN.getName())
                && userPermissions.contains(UserSubType.ADMIN.getName())) {
            return ResponseEntity.ok().build();
        }
        boolean inWork = workServ.isInWork(user);
        return inWork
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt w Klubie");
    }


    public ResponseEntity<String> addRecordToChangeHistory(String pinCode, String classNamePlusMethod, String uuid) throws NoUserPermissionException {
        String pin = Hashing.sha256()
                .hashString(pinCode, StandardCharsets.UTF_8)
                .toString();
        UserEntity user = userRepository.findByPinCode(pin).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Brak użytkownika");
        }
        if (!workServ.isInWork(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Najpierw zarejestruj pobyt w Klubie");
        }
        List<String> permissions = user.getUserPermissionsList();
        boolean hasPermission = permissions != null && (
                permissions.contains(UserSubType.MANAGEMENT.getName()) ||
                        permissions.contains(UserSubType.WORKER.getName())
        );
        if (!hasPermission) {
            throw new NoUserPermissionException();
        }
        user.getList().add(addRecord(user, classNamePlusMethod, uuid));
        userRepository.save(user);
        return ResponseEntity.ok("Zapisano historię zmian");
    }


}

