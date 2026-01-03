package com.shootingplace.shootingplace.member.permissions;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberInfo;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
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
public class MemberPermissionsController {

    private final PermissionService permissionService;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
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

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateMemberPermissions(@PathVariable String memberUUID,
                                                     @RequestBody MemberPermissions memberPermissions) {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));
        return permissionService.updatePermissions(member.getMemberPermissions(), memberPermissions);
    }
    @PutMapping("/{otherID}")
    public ResponseEntity<?> updateOtherPermissions(@PathVariable Integer otherID,
                                                     @RequestBody MemberPermissions memberPermissions) {
        OtherPersonEntity otherPersonEntity = otherPersonRepository.findById(otherID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono osoby"));
        return permissionService.updatePermissions(otherPersonEntity.getPermissionsEntity(), memberPermissions);
    }

    @PutMapping("arbiter/{memberUUID}")
    public ResponseEntity<?> updateMemberArbiterClass(@PathVariable String memberUUID) {
        return permissionService.updateMemberArbiterStaticClass(memberUUID);
    }
}
