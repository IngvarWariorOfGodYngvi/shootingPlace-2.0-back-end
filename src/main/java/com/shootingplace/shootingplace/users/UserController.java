package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO userCreateDTO, @RequestParam String superPinCode) {
        return userService.createUser(userCreateDTO, superPinCode);
    }

    @PostMapping("/editUser")
    public ResponseEntity<?> editUser(@RequestBody UserCreateDTO userCreateDTO, @RequestParam String superPinCode, @RequestParam String userUUID) {
        return userService.editUser(userCreateDTO, superPinCode, userUUID);
    }

    @DeleteMapping("/delete")
    @RequirePermissions({UserSubType.SUPER_USER, UserSubType.ADMIN, UserSubType.CEO})
    public ResponseEntity<?> deleteUser(@RequestParam String userID) {
        return userService.deleteUser(userID);
    }
}

