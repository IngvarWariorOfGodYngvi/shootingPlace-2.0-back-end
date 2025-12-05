package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final ChangeHistoryService changeHistoryService;

    public UserController(UserService userService, ChangeHistoryService changeHistoryService) {
        this.userService = userService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/getAccess")
    public ResponseEntity<?> getAccess(@RequestParam String pinCode) throws NoUserPermissionException {
        return userService.getAccess(pinCode);
    }

    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissions() {
        return ResponseEntity.ok(userService.getPermissions());
    }

    @GetMapping("/permissionsByPin")
    public ResponseEntity<?> permissionsByPin(@RequestParam String pinCode) {
        return ResponseEntity.ok(userService.permissionsByPin(pinCode));
    }

    @GetMapping("/userList")
    public ResponseEntity<?> getListOfUser() {
        return ResponseEntity.ok(userService.getListOfUser());
    }

    @GetMapping("/userActions")
    public ResponseEntity<?> getUserActions(@RequestParam String uuid) {
        return userService.getUserActions(uuid);
    }


    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestParam String firstName, @RequestParam String secondName, @RequestParam List<String> userPermissionsList, @RequestParam String pinCode, @RequestParam String superPinCode, @RequestParam @Nullable String memberUUID, @RequestParam @Nullable Integer otherID) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.ADMIN.getName(), UserSubType.SUPER_USER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(superPinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return userService.createUser(firstName, secondName, userPermissionsList, pinCode, superPinCode, memberUUID, otherID);
        }
        return code;
    }

    @PostMapping("/editUser")
    public ResponseEntity<?> editUser(@Nullable @RequestParam String firstName, @Nullable @RequestParam String secondName, @Nullable @RequestParam List<String> userPermissionsList, @Nullable @RequestParam String pinCode, @RequestParam String superPinCode, @RequestParam @Nullable String memberUUID, @RequestParam @Nullable String otherID, @RequestParam String userUUID) throws NoUserPermissionException {
        if (pinCode == null || pinCode.isEmpty() || pinCode.equals("null")) {
            pinCode = null;
        }
        List<String> acceptedPermissions = Arrays.asList(UserSubType.ADMIN.getName(), UserSubType.SUPER_USER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(superPinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return userService.editUser(firstName, secondName, userPermissionsList, pinCode, superPinCode, memberUUID, otherID, userUUID);
        }
        return code;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestParam String userID, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.ADMIN.getName(), UserSubType.SUPER_USER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return userService.deleteUser(userID, pinCode);
        }
        return code;
    }

}
