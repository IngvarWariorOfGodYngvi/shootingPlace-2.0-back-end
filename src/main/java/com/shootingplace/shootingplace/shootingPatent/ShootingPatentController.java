package com.shootingplace.shootingplace.shootingPatent;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patent")
@CrossOrigin
@RequiredArgsConstructor
public class ShootingPatentController {

    private final ShootingPatentService shootingPatentService;

    @GetMapping("/")
    public ResponseEntity<?> getMembersWithNoShootingPatent(){
        return ResponseEntity.ok(shootingPatentService.getMembersWithNoShootingPatent());
    }
    @GetMapping("/noLicense")
    public ResponseEntity<?> getMembersWithShootingPatentAndNoLicense(){
        return ResponseEntity.ok(shootingPatentService.getMembersWithShootingPatentAndNoLicense());
    }

    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updatePatent(@PathVariable String memberUUID, @RequestBody ShootingPatent shootingPatent) {
        return shootingPatentService.updatePatent(memberUUID, shootingPatent);
    }


}
