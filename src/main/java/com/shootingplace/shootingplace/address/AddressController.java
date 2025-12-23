package com.shootingplace.shootingplace.address;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
@CrossOrigin
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Transactional
    @PutMapping("/{memberUUID}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> updateMemberAddress(@PathVariable String memberUUID, @RequestBody Address address, @RequestParam String pinCode) {
            return addressService.updateAddress(memberUUID, address);
    }
}