package com.shootingplace.shootingplace.weaponPermission;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weapon")
@CrossOrigin
public class WeaponController {

    private final WeaponPermissionService weaponPermissionService;

    public WeaponController(WeaponPermissionService weaponPermissionService) {
        this.weaponPermissionService = weaponPermissionService;
    }

    @PutMapping("/weapon/{memberUUID}")
    public ResponseEntity<?> changeWeaponPermission(@PathVariable String memberUUID, @RequestBody WeaponPermission weaponPermission) {
        return weaponPermissionService.updateWeaponPermission(memberUUID, weaponPermission);
    }

    @PatchMapping("/weapon/{memberUUID}")
    public ResponseEntity<?> removeWeaponPermission(@PathVariable String memberUUID, @RequestParam boolean admission, @RequestParam boolean permission) {
        return weaponPermissionService.removeWeaponPermission(memberUUID, admission, permission);
    }

}
