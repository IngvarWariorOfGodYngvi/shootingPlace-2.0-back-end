package com.shootingplace.shootingplace.settings;

import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.club.ClubService;
import com.shootingplace.shootingplace.configurations.UpdateService;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
@CrossOrigin
@RequiredArgsConstructor
public class SettingsController {

    private final ClubService clubService;
    private final ApplicationLicenseService applicationLicenseService;
    private final UpdateService updateService;
    private final SystemConfigRepository systemConfigRepository;

    @Transactional
    @PostMapping("/createMotherClub")
    public ResponseEntity<?> createMotherClub(@RequestBody Club club) {
        return clubService.createMotherClub(club);
    }

    @Transactional
    @PostMapping("/changeMode")
    @RequirePermissions({UserSubType.MANAGEMENT, UserSubType.ADMIN, UserSubType.SUPER_USER})
    public ResponseEntity<?> changeMode() {
        return ResponseEntity.ok("Zmieniono tryb pracy");
    }

    @GetMapping("/termsAndLicense")
    public ResponseEntity<?> termsAndLicense() {

        LocalDate endLicense = applicationLicenseService.getEndDate();
        boolean isEnd = applicationLicenseService.isExpired();

        Map<String, String> map = new HashMap<>();
        map.put("message", !isEnd ? "Licencja na program jest ważna do: " + endLicense : "Licencja skończyła się: " + endLicense);
        map.put("isEnd", String.valueOf(isEnd));
        map.put("endDate", String.valueOf(endLicense));

        return isEnd ? ResponseEntity.badRequest().body(map) : ResponseEntity.ok(map);
    }

    @PostMapping("/update")
    @RequirePermissions(value = {UserSubType.ADMIN, UserSubType.SUPER_USER, UserSubType.CEO})
    public ResponseEntity<?> updateProgram() {
        updateService.startUpdateAgent();
        return ResponseEntity.ok().build();

    }
    @PostMapping("/addCalendar")
    @RequirePermissions(value = {UserSubType.ADMIN, UserSubType.SUPER_USER, UserSubType.CEO, UserSubType.MANAGEMENT})
    public ResponseEntity<?> addCalendar(@RequestBody String iframe) {
        SystemConfigEntity config = systemConfigRepository.findById(1).orElseThrow(() -> new DomainNotFoundException("Config", "1"));
        config.setIFrameGoogleCalendar(iframe);
        systemConfigRepository.save(config);
        return ResponseEntity.ok().body("Zapisano kalendarz");
    }
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar() {
        return ResponseEntity.ok().body(systemConfigRepository.findById(1).orElseThrow(() -> new DomainNotFoundException("Config", "1")).getIFrameGoogleCalendar());
    }
}
