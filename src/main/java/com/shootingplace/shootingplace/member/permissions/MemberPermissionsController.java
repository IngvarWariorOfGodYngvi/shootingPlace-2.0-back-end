package com.shootingplace.shootingplace.member.permissions;

import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonService;
import com.shootingplace.shootingplace.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@CrossOrigin
@RequiredArgsConstructor
public class MemberPermissionsController {

    private final MemberPermissionsService memberPermissionsService;
    private final OtherPersonService otherPersonService;
    private final UserService userService;

    @GetMapping("/othersWithPermissions")
    public List<OtherPersonEntity> getOthersWithPermissions() {
        return otherPersonService.getOthersWithPermissions();
    }

    @GetMapping("/getArbiterClasses")
    public ResponseEntity<?> getArbiterClasses() {
        return ResponseEntity.ok(memberPermissionsService.getArbiterClasses());
    }

    @GetMapping("/checkArbiter")
    public ResponseEntity<?> checkArbiterByCode(@RequestParam String code) {
        return userService.checkArbiterByCode(code);
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateMemberPermissions(@PathVariable String memberUUID,
                                                     @RequestBody MemberPermissions memberPermissions, @RequestParam String ordinal) {
        return memberPermissionsService.updateMemberPermissions(memberUUID, memberPermissions, ordinal);
    }

    @PutMapping("arbiter/{memberUUID}")
    public ResponseEntity<?> updateMemberArbiterClass(@PathVariable String memberUUID) {
        return memberPermissionsService.updateMemberArbiterClass(memberUUID);
    }
}
