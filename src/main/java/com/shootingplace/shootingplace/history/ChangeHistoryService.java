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

    public ResponseEntity<?> comparePinCode(String pinCode, List<String> acceptedPermissions) throws NoUserPermissionException {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin);
        if (userEntity != null && userEntity.getUserPermissionsList().stream().noneMatch(acceptedPermissions::contains)) {
            throw new NoUserPermissionException();
        }
        if (userEntity != null && acceptedPermissions.contains(UserSubType.ADMIN.getName()) && userEntity.getUserPermissionsList().contains(UserSubType.ADMIN.getName())) {
            return ResponseEntity.ok().build();
        }
        boolean inWork;
        if (userEntity != null) {
            inWork = workServ.isInWork(userEntity);
        } else {
            inWork = false;
        }
        return inWork ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt w Klubie");

    }

    public ResponseEntity<String> addRecordToChangeHistory(String pinCode, String classNamePlusMethod, String uuid) throws NoUserPermissionException {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin);
        // user found
        if (userEntity != null) {
            // user isn't in work
            if (!workServ.isInWork(userEntity)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Najpierw zarejestruj pobyt w Klubie");
            }
            if (userEntity.getUserPermissionsList().contains(UserSubType.MANAGEMENT.getName()) || userEntity.getUserPermissionsList().contains(UserSubType.WORKER.getName())) {
                userEntity.getList().add(addRecord(userEntity, classNamePlusMethod, uuid));
                userRepository.save(userEntity);
                return null;
            } else {
                throw new NoUserPermissionException();
            }
        }
        //user not found
        else {
            return ResponseEntity.status(403).body("Brak Użytkownika");
        }
    }

}

