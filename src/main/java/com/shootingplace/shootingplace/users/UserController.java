package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/access")
    @RequirePermissions({UserSubType.MANAGEMENT, UserSubType.ADMIN, UserSubType.SUPER_USER})
    public ResponseEntity<?> getAccess() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<String>> getPermissions() {
        return ResponseEntity.ok(userService.getPermissions());
    }

    @GetMapping("/userList")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.getListOfUser());
    }

    @GetMapping("/userActions")
    public ResponseEntity<?> getUserActions(@RequestParam String uuid) {
        return userService.getUserActions(uuid);
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> createUser(@RequestParam String firstName, @RequestParam String secondName, @RequestParam List<String> userPermissionsList, @RequestParam String pinCode, @RequestParam String superPinCode, @RequestParam @Nullable String memberUUID, @RequestParam @Nullable Integer otherID) {

        return userService.createUser(firstName, secondName, userPermissionsList, pinCode, superPinCode, memberUUID, otherID);
    }

    @PostMapping("/editUser")
    public ResponseEntity<?> editUser(@Nullable @RequestParam String firstName, @Nullable @RequestParam String secondName, @Nullable @RequestParam List<String> userPermissionsList, @Nullable @RequestParam String pinCode, @RequestParam String superPinCode, @RequestParam @Nullable String memberUUID, @RequestParam @Nullable String otherID, @RequestParam String userUUID) {
        return userService.editUser(firstName, secondName, userPermissionsList, pinCode, superPinCode, memberUUID, otherID, userUUID);
    }

    @DeleteMapping("/delete")
    @RequirePermissions({UserSubType.SUPER_USER, UserSubType.ADMIN, UserSubType.CEO})
    public ResponseEntity<?> deleteUser(@RequestParam String userID) {
        return userService.deleteUser(userID);
    }
}

