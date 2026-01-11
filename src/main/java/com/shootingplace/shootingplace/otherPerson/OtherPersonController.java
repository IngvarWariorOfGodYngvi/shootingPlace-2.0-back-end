package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/other")
@CrossOrigin
@RequiredArgsConstructor
public class OtherPersonController {

    private final OtherPersonService otherPersonService;

    @Transactional
    @PostMapping("")
    public ResponseEntity<?> addPerson(@RequestBody OtherPerson person) {
        return otherPersonService.addPerson(person);
    }

    @GetMapping("/")
    public ResponseEntity<List<String>> getAllOthers() {
        return ResponseEntity.ok().body(otherPersonService.getAllOthers());
    }

    @GetMapping("/getOtherByPhone/{phone}")
    public ResponseEntity<?> getOtherByPhone(@PathVariable String phone) {
        return otherPersonService.getOtherByPhone(phone);
    }
    @GetMapping("/getOtherByLicense/{license}")
    public ResponseEntity<?> getOtherByLicense(@PathVariable String license) {
        return otherPersonService.getOtherByLicense(license);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok().body(otherPersonService.getAll());
    }

    @GetMapping("/allCompetitors")
    public ResponseEntity<?> allCompetitors() {
        return ResponseEntity.ok().body(otherPersonService.getAllCompetitors());
    }

    @DeleteMapping("/deactivatePerson")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> deactivatePerson(@RequestParam int id) {
        return otherPersonService.deactivatePerson(id);
    }

    @PutMapping("/")
    public ResponseEntity<?> updatePerson(@RequestParam String id, @RequestBody OtherPerson otherPerson) {
        return otherPersonService.updatePerson(id, otherPerson);
    }
}
