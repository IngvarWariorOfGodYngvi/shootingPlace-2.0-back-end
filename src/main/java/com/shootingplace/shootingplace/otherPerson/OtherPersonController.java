package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.enums.ArbiterClass;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.member.MemberInfo;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/other")
@CrossOrigin
@RequiredArgsConstructor
public class OtherPersonController {

    private final OtherPersonService otherPersonService;

    @PostMapping("")
    public ResponseEntity<?> addPerson(@RequestBody OtherPerson person, @RequestParam String club,
                                       @Nullable @RequestParam String arbiterClass,
                                       @Nullable @RequestParam String arbiterNumber,
                                       @Nullable @RequestParam String arbiterPermissionValidThru) {
        MemberPermissions memberPermissions = null;
        if (arbiterClass != null && !arbiterClass.isEmpty()) {
            if (arbiterClass.equals("1")) {
                arbiterClass = (ArbiterClass.CLASS_3.getName());
            }
            if (arbiterClass.equals("2")) {
                arbiterClass = (ArbiterClass.CLASS_2.getName());
            }
            if (arbiterClass.equals("3")) {
                arbiterClass = (ArbiterClass.CLASS_1.getName());
            }
            if (arbiterClass.equals("4")) {
                arbiterClass = (ArbiterClass.CLASS_STATE.getName());
            }
            if (arbiterClass.equals("5")) {
                arbiterClass = (ArbiterClass.CLASS_INTERNATIONAL.getName());
            }
            LocalDate parse = null;
            if (!Objects.equals(arbiterPermissionValidThru, "")) {
                if (arbiterPermissionValidThru != null) {
                    parse = LocalDate.parse(arbiterPermissionValidThru);
                }
            }
            memberPermissions = MemberPermissions.builder()
                    .arbiterStaticNumber(arbiterNumber)
                    .arbiterStaticClass(arbiterClass)
                    .arbiterStaticPermissionValidThru(parse)
                    .shootingLeaderNumber(null)
                    .instructorNumber(null)
                    .build();
        }
        if (club.isEmpty()) {
            club = "BRAK";
        }
        return otherPersonService.addPerson(club, person, memberPermissions);
    }

    @GetMapping("/")
    public ResponseEntity<List<String>> getAllOthers() {
        return ResponseEntity.ok().body(otherPersonService.getAllOthers());
    }

    @GetMapping("/arbiters")
    public List<MemberInfo> getAllOthersArbiters() {
        return otherPersonService.getAllOthersArbiters();
    }

    @GetMapping("/getOtherByPhone/{phone}")
    public ResponseEntity<?> getOtherByPhone(@PathVariable String phone) {
        return otherPersonService.getOtherByPhone(phone);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok().body(otherPersonService.getAll());
    }

    @DeleteMapping ("/deactivatePerson")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> deactivatePerson(@RequestParam int id, @RequestParam String pinCode) {
            return otherPersonService.deactivatePerson(id);
    }

    @PutMapping("/")
    public ResponseEntity<?> updatePerson(@RequestParam String id,@RequestBody OtherPerson otherPerson,@RequestParam String clubName) {
        return otherPersonService.updatePerson(id, otherPerson, clubName);
    }
}
