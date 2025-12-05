package com.shootingplace.shootingplace.barCodeCards;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

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
    public ResponseEntity<?> deactivateCard(@RequestParam String barCode, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName(), UserSubType.SUPER_USER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return barCodeCardService.deactivateCard(barCode, pinCode);
        }
        return code;
    }

}
