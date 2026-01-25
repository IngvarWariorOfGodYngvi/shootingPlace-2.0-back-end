package com.shootingplace.shootingplace.soz;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/soz")
@CrossOrigin
@RequiredArgsConstructor
public class SozController {

    private final SozService sozService;

    @GetMapping("/")
    public ResponseEntity<?> getSozConfig() {
        return sozService.getSozConfig();
    }

    @GetMapping("/invitations")
    public ResponseEntity<?> getInvitations() {
        return sozService.getInvitations();
    }
    @GetMapping("/members")
    public ResponseEntity<?> getMembers() {
        return sozService.getMembers();
    }
    @GetMapping("/licenseAndPatents")
    public ResponseEntity<?> getLicenseAndPatents() {
        return sozService.getLicenseAndPatents();
    }

    @PostMapping("/")
    @RequirePermissions(value = {UserSubType.ADMIN, UserSubType.SUPER_USER, UserSubType.CEO, UserSubType.MANAGEMENT})
    public ResponseEntity<?> addSozConfig(@RequestBody SozConfig sozConfig) {
        return sozService.setSozConfig(sozConfig);
    }
}
