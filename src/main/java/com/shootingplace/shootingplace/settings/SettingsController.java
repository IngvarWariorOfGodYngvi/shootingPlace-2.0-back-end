package com.shootingplace.shootingplace.settings;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.club.ClubService;
import com.shootingplace.shootingplace.configurations.UpdateService;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
@CrossOrigin
@RequiredArgsConstructor
public class SettingsController {

    private final ClubService clubService;
    private final UserRepository userRepository;
    private final ApplicationLicenseService applicationLicenseService;

    @Transactional
    @PostMapping("/createMotherClub")
    public ResponseEntity<?> createMotherClub(@RequestBody Club club) {
        return clubService.createMotherClub(club);
    }

    @Transactional
    @PostMapping("/changeMode")
    public ResponseEntity<?> changeMode(@RequestParam String pinCode) {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin).orElseThrow(EntityNotFoundException::new);
        if (userEntity.getUserPermissionsList().contains(UserSubType.ADMIN.getName()) || userEntity.getUserPermissionsList().contains(UserSubType.SUPER_USER.getName()) || userEntity.getUserPermissionsList().contains(UserSubType.CEO.getName())) {
            return ResponseEntity.ok("Zmieniono tryb pracy");
        }
        return ResponseEntity.badRequest().body("Brak Uprawnień");

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

    @Transactional
    @PostMapping("/update")
    @RequirePermissions(value = {UserSubType.ADMIN, UserSubType.SUPER_USER, UserSubType.CEO})
    public ResponseEntity<?> updateProgram(@RequestParam String pinCode) {
        new UpdateService().startUpdateAgent();
        return ResponseEntity.ok().build();

    }

}
