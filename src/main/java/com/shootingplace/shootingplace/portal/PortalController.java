package com.shootingplace.shootingplace.portal;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal")
@CrossOrigin
@RequiredArgsConstructor
public class PortalController {
    private final PortalExportService portalExportService;

    @PostMapping("/export")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> export(@RequestParam String tournamentUUID) {

        portalExportService.exportTournament(tournamentUUID);

        return ResponseEntity.ok("Eksport zakończony powodzeniem");
    }
}
