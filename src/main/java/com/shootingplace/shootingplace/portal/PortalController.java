package com.shootingplace.shootingplace.portal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal")
@CrossOrigin
@RequiredArgsConstructor
public class PortalController {

    @PostMapping("/export")
    public ResponseEntity<?> export(@RequestParam String tournamentUUID) {
        return ResponseEntity.ok("udaję, że wysyłam export " + tournamentUUID);
    }
}
