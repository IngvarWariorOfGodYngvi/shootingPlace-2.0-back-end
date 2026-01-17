package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.settings.SystemConfigRepository;
import com.shootingplace.shootingplace.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/conf")
@CrossOrigin
@RequiredArgsConstructor
public class ConfigController {

    private final Environment environment;
    private final UserService userService;
    private final SystemConfigRepository systemConfigRepository;
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(systemConfigRepository.findById(1).orElseThrow(() -> new DomainNotFoundException("SystemConfig", "1")).getBuildTime());
    }

    @GetMapping("/env")
    public ResponseEntity<?> env() {
        List<String> env = new ArrayList<>();
        env.add(environment.getActiveProfiles()[0]);
        env.add(environment.getProperty("shootingPlaceName"));
        return ResponseEntity.ok(env);
    }

    @GetMapping("/fs")
    public ResponseEntity<?> fs() {
        return userService.checkFirstStart();
    }
}
