package com.shootingplace.shootingplace.barCodeCards;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.security.RequirePermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/barCode")
@CrossOrigin
public class BarCodeCardController {

    private final BarCodeCardService barCodeCardService;
    private final ChangeHistoryService changeHistoryService;

    public BarCodeCardController(BarCodeCardService barCodeCardService, ChangeHistoryService changeHistoryService) {
        this.barCodeCardService = barCodeCardService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/")
    public ResponseEntity<?> findMemberByCard(@RequestParam String cardNumber) {
        return barCodeCardService.findMemberByCard(cardNumber);
    }

    @PostMapping("/")
    public ResponseEntity<?> addNewCardToPerson(@RequestParam String barCode, @RequestParam String uuid, @RequestParam String pinCode) throws NoUserPermissionException {
        return barCodeCardService.createNewCard(barCode, uuid, pinCode);
    }
    @PutMapping("/")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.SUPER_USER})
    public ResponseEntity<?> deactivateCard(@RequestParam String barCode, @RequestParam String pinCode) throws NoUserPermissionException {
            return barCodeCardService.deactivateCard(barCode, pinCode);
    }

}
