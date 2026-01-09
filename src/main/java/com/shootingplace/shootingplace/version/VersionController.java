package com.shootingplace.shootingplace.version;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @GetMapping
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok(versionService.getCurrentVersion());
    }
}
