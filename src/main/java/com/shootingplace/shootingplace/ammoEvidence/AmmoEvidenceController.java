package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.armory.AmmoUsedService;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoPersonToAmmunitionException;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ammoEvidence")
@CrossOrigin
public class AmmoEvidenceController {

    private final AmmoEvidenceService ammoEvidenceService;
    private final AmmoUsedService ammoUsedService;
    private final ChangeHistoryService changeHistoryService;

    public AmmoEvidenceController(AmmoEvidenceService ammoEvidenceService, AmmoUsedService ammoUsedService, ChangeHistoryService changeHistoryService) {
        this.ammoEvidenceService = ammoEvidenceService;
        this.ammoUsedService = ammoUsedService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/isEvidenceIsClosed")
    public ResponseEntity<?> isEvidenceIsClosedOrEqual(@RequestParam(required = false) int quantity) {
        return ResponseEntity.ok(ammoUsedService.isEvidenceIsClosedOrEqual(quantity));
    }

    @GetMapping("/personalAmmoFromList")
    public ResponseEntity<?> getPersonalAmmoFromList(@Nullable @RequestParam String legitimationNumber, @Nullable @RequestParam String IDNumber, @RequestParam String evidenceUUID) {

        return ResponseEntity.ok(ammoUsedService.getPersonalAmmoFromList(legitimationNumber, IDNumber, evidenceUUID));
    }

    @GetMapping("/evidence")
    public ResponseEntity<?> getOpenEvidence() {
        return ammoEvidenceService.getOpenEvidence();
    }

    @GetMapping("/oneEvidence")
    public ResponseEntity<?> getEvidence(@RequestParam String uuid) {
        return ResponseEntity.ok(ammoEvidenceService.getEvidence(uuid));
    }
    @GetMapping("/notLockedEvidences")
    public ResponseEntity<?> getNotLockedEvidences() {
        return ResponseEntity.ok(ammoEvidenceService.getNotLockedEvidences());
    }

    @GetMapping("/closedEvidences")
    public ResponseEntity<List<AmmoDTO>> getClosedEvidence(Pageable page) {
        return ResponseEntity.ok().body(ammoEvidenceService.getClosedEvidences(page));
    }

    @GetMapping("/checkAnyOpenEvidence")
    public ResponseEntity<?> checkAnyOpenEvidence() {
        return ResponseEntity.ok().body(ammoEvidenceService.checkAnyOpenEvidence());
    }
    @GetMapping("/getAmmoInEvidece")
    public ResponseEntity<?> getAmmoInEvidece(String caliberUUID) {
        return ResponseEntity.ok().body(ammoEvidenceService.getAmmoInEvidece(caliberUUID));
    }

    @Transactional
    @PostMapping("/ammo")
    public ResponseEntity<?> createAmmoUsed(@RequestParam String caliberUUID, @RequestParam Integer legitimationNumber, @RequestParam int otherID, @RequestParam Integer counter) throws NoPersonToAmmunitionException {
        if (counter != 0) {
            return ammoUsedService.addAmmoUsedEntity(caliberUUID, legitimationNumber, otherID, counter);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Transactional
    @PostMapping("/listOfAmmo")
    public ResponseEntity<?> createAmmoUsed(@RequestBody Map<String, String> caliberUUIDAmmoQuantityMap, @RequestParam Integer legitimationNumber, @RequestParam Integer otherID) throws NoPersonToAmmunitionException {
        boolean[] caliberAmmocheck = new boolean[caliberUUIDAmmoQuantityMap.size()];
        final int[] iterator = {0};
        caliberUUIDAmmoQuantityMap.forEach((key, value) -> {
            caliberAmmocheck[iterator[0]] = value != null && Integer.parseInt(value) != 0;
            iterator[0]++;
        });
        boolean check = true;
        for (boolean b : caliberAmmocheck) {
            if (!b) {
                check = false;
                break;
            }
        }
        if (otherID == null || otherID == 0) {
            otherID = null;
        }
        if (check) {
            return ammoUsedService.addListOfAmmoToEvidence(caliberUUIDAmmoQuantityMap, legitimationNumber, otherID);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @PatchMapping("/ammo")
    public ResponseEntity<?> closeEvidence(@RequestParam String evidenceUUID) {
        return ammoEvidenceService.closeEvidence(evidenceUUID);
    }

    @Transactional
    @PatchMapping("/ammoOpen")
    public ResponseEntity<?> openEvidence(@RequestParam String pinCode, @RequestParam String evidenceUUID) throws NoUserPermissionException {
        System.out.println("wchodzę");
        System.out.println(pinCode);
        System.out.println(evidenceUUID);
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName(),UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return ammoEvidenceService.openEvidence(evidenceUUID, pinCode);
        } else {
            return code;
        }
    }

}
