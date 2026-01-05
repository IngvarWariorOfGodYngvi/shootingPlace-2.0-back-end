package com.shootingplace.shootingplace.permissions;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberInfo;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonService;
import com.shootingplace.shootingplace.users.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@CrossOrigin
@RequiredArgsConstructor
public class PermissionsController {

    private final PermissionService permissionService;
    private final MemberRepository memberRepository;
    private final OtherPersonService otherPersonService;
    private final UserService userService;

    @GetMapping("/othersWithPermissions")
    public List<OtherPersonEntity> getOthersWithPermissions() {
        return otherPersonService.getOthersWithPermissions();
    }

    @GetMapping("/getArbiterStaticClasses")
    public ResponseEntity<?> getArbiterStaticClasses() {
        return ResponseEntity.ok(permissionService.getArbiterStaticClasses());
    }
    @GetMapping("/getArbiterDynamicClasses")
    public ResponseEntity<?> getArbiterDynamicClasses() {
        return ResponseEntity.ok(permissionService.getArbiterDynamicClasses());
    }

    @GetMapping("/checkArbiter")
    public ResponseEntity<?> checkArbiterByCode(@RequestParam String code) {
        return userService.checkArbiterByCode(code);
    }
    @GetMapping("/getArbiters")
    public List<MemberInfo> getArbiters() {
        return permissionService.getArbiters();
    }

    @GetMapping("/arbiters")
    public List<MemberInfo> getAllOthersArbiters() {
        return permissionService.getAllOthersArbiters();
    }

    @PutMapping("/member/{memberUUID}")
    public ResponseEntity<?> updateMemberPermissions(@PathVariable String memberUUID,
                                                     @RequestBody MemberPermissions memberPermissions) {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));
        return permissionService.updatePermissions(member.getMemberPermissions(), memberPermissions);
    }

    @PutMapping("arbiter/static/{memberUUID}")
    public ResponseEntity<?> updateArbiterStaticClass(@RequestBody MemberPermissions permissions,@PathVariable String memberUUID) {
        return permissionService.updateMemberArbiterStaticClass(permissions,memberUUID);
    }
    @PutMapping("arbiter/dynamic/{memberUUID}")
    public ResponseEntity<?> updateArbiterDynamicClass(@RequestBody MemberPermissions permissions,@PathVariable String memberUUID) {
        return permissionService.updateMemberArbiterDynamicClass(permissions,memberUUID);
    }
}
