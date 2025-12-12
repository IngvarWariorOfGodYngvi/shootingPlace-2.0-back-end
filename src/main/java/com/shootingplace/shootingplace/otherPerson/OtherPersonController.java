package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.enums.ArbiterClass;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.member.MemberInfo;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/other")
@CrossOrigin
public class OtherPersonController {
    private final OtherPersonService otherPersonService;
    private final ChangeHistoryService changeHistoryService;

    public OtherPersonController(OtherPersonService otherPersonService, ChangeHistoryService changeHistoryService) {
        this.otherPersonService = otherPersonService;
        this.changeHistoryService = changeHistoryService;
    }

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
                    .arbiterNumber(arbiterNumber)
                    .arbiterClass(arbiterClass)
                    .arbiterPermissionValidThru(parse)
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
    public ResponseEntity<?> deactivatePerson(@RequestParam int id, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return otherPersonService.deactivatePerson(id, pinCode);
        }
        return code;
    }

    @PutMapping("/")
    public ResponseEntity<?> updatePerson(@RequestParam String id,@RequestBody OtherPerson otherPerson,@RequestParam String clubName) {
        return otherPersonService.updatePerson(id, otherPerson, clubName);
    }
}
