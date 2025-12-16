package com.shootingplace.shootingplace.address;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.security.RequirePermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
@CrossOrigin
public class AddressController {

    private final AddressService addressService;
    private final ChangeHistoryService changeHistoryService;

    public AddressController(AddressService addressService, ChangeHistoryService changeHistoryService) {
        this.addressService = addressService;
        this.changeHistoryService = changeHistoryService;
    }

    @Transactional
    @PutMapping("/{memberUUID}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> updateMemberAddress(@PathVariable String memberUUID, @RequestBody Address address, @RequestParam String pinCode) throws NoUserPermissionException {
            return addressService.updateAddress(memberUUID, address, pinCode);
    }
}