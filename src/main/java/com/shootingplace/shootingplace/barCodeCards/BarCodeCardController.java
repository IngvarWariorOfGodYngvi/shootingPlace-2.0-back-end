package com.shootingplace.shootingplace.barCodeCards;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/barCode")
@CrossOrigin
@RequiredArgsConstructor
public class BarCodeCardController {

    private final BarCodeCardService barCodeCardService;

    @GetMapping("/")
    public ResponseEntity<?> findMemberByCard(@RequestParam String cardNumber) {
        return barCodeCardService.findMemberByCard(cardNumber);
    }

    @PostMapping("/")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER,UserSubType.ADMIN, UserSubType.SUPER_USER})
    public ResponseEntity<?> addNewCardToPerson(@RequestParam String barCode, @RequestParam String uuid) {
        return barCodeCardService.createNewCard(barCode, uuid);
    }
    @PutMapping("/")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.SUPER_USER})
    public ResponseEntity<?> deactivateCard(@RequestParam String barCode) {
            return barCodeCardService.deactivateCard(barCode);
    }

}
