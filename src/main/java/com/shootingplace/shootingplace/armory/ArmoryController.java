package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.file.FilesService;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/armory")
@CrossOrigin
public class ArmoryController {

    private final ArmoryService armoryService;
    private final CaliberService caliberService;
    private final AmmoUsedService ammoUsedService;
    private final ChangeHistoryService changeHistoryService;
    private final ShootingPacketService shootingPacketService;
    private final FilesService filesService;


    public ArmoryController(ArmoryService armoryService, CaliberService caliberService, AmmoUsedService ammoUsedService, ChangeHistoryService changeHistoryService, ShootingPacketService shootingPacketService, FilesService filesService) {
        this.armoryService = armoryService;
        this.caliberService = caliberService;
        this.ammoUsedService = ammoUsedService;
        this.changeHistoryService = changeHistoryService;
        this.shootingPacketService = shootingPacketService;
        this.filesService = filesService;
    }

    @GetMapping("/recount")
    public void recount() {
        ammoUsedService.recountAmmo();
    }

    @GetMapping("/getCaiberNameFromCaliberUUID")
    public ResponseEntity<?> getCaiberNameFromCaliberUUID(@RequestParam String caliberUUID) {
        return ResponseEntity.ok(caliberService.getCaliberNameFromCaliberUUID(caliberUUID));
    }

    @GetMapping("/getAllGunUsedIssuance")
    public ResponseEntity<?> getAllGunUsedIssuance() {
        return ResponseEntity.ok(armoryService.getAllGunUsedIssuance());
    }

    @GetMapping("/getAllGunUsedAcceptance")
    public ResponseEntity<?> getAllGunUsedAcceptance() {
        return ResponseEntity.ok(armoryService.getAllGunUsedAcceptance());
    }

    @GetMapping("/getAllGunUsed")
    public ResponseEntity<?> getAllGunUsed(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(armoryService.getAllGunUsed(parseFirstDate,parseSecondDate));
    }

    @GetMapping("/calibers")
    public ResponseEntity<?> getCalibersList() {
        return ResponseEntity.ok(caliberService.getCalibersList());
    }

    @GetMapping("/calibersForEvidence")
    public ResponseEntity<?> calibersForEvidence() {
        return ResponseEntity.ok(caliberService.calibersForEvidence());
    }

    @GetMapping("/caliberQuantity")
    public ResponseEntity<?> getcalibersQuantity(@RequestParam String uuid, @RequestParam String date) {
        LocalDate parseDate = LocalDate.parse(date);
        return ResponseEntity.ok(caliberService.getCalibersQuantity(uuid, parseDate));
    }

    @GetMapping("/getGun")
    public ResponseEntity<?> getGun(@RequestParam String gunUUID) {
        return armoryService.getGun(gunUUID);
    }

    @GetMapping("/getGunUsedByUUID")
    public ResponseEntity<?> getGunUsedByUUID(@RequestParam String gunUsedUUID) {
        return armoryService.getGunUsedByUUID(gunUsedUUID);
    }

    // jest nadal w użyciu na liście amunicyjnej
    @GetMapping("/getGunList")
    public ResponseEntity<?> getGunList() {
        return ResponseEntity.ok(armoryService.getGunList());
    }

    // Lista amunicyjna
    @GetMapping("/getGunListAmmoList")
    public ResponseEntity<?> getGunUsedListAmmoList() {
        return ResponseEntity.ok(armoryService.getGunUsedListAmmoList());
    }

    @Transactional
    @GetMapping("/quantitySum")
    public ResponseEntity<?> getSumFromAllAmmoList(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(armoryService.getSumFromAllAmmoList(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/gunType")
    public ResponseEntity<List<GunStoreEntity>> getGunTypeList() {
        return ResponseEntity.ok(armoryService.getGunTypeList());
    }

    @GetMapping("/getGuns")
    public ResponseEntity<?> getAllGuns() {
        return ResponseEntity.ok(armoryService.getAllGuns());
    }

    @GetMapping("/getRemovedGuns")
    public ResponseEntity<?> getAllRemovedGuns() {
        return ResponseEntity.ok(armoryService.getAllRemovedGuns());
    }

    @GetMapping("/getHistory")
    public ResponseEntity<?> getHistoryOfCaliber(@RequestParam String caliberUUID) {
        return ResponseEntity.ok(armoryService.getHistoryOfCaliber(caliberUUID));
    }

    @GetMapping("/getGunByBarcode")
    public ResponseEntity<?> findGunByBarcode(@RequestParam String barcode) {
        return ResponseEntity.ok(armoryService.findGunByBarcode(barcode));
    }

    @GetMapping("/getGunUsedHistory")
    public ResponseEntity<?> getGunUsedHistory(@RequestParam String gunUUID) {

        return ResponseEntity.ok(armoryService.getGunUsedHistory(gunUUID));
    }

    @GetMapping("/getAllShootingPacket")
    public ResponseEntity<?> getAllShootingPacket() {
        return ResponseEntity.ok(shootingPacketService.getAllShootingPacket());
    }

    @GetMapping("/getAllShootingPacketEntities")
    public ResponseEntity<?> getAllShootingPacketEntities() {
        return ResponseEntity.ok(shootingPacketService.getAllShootingPacketEntities());
    }

    @Transactional
    @PostMapping("/addShootingPacket")
    public ResponseEntity<?> addShootingPacket(@RequestParam String name, @RequestParam float price, @RequestBody Map<String, Integer> map, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName(), UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return shootingPacketService.addShootingPacket(name, price, map, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @DeleteMapping("/deleteShootingPacket")
    public ResponseEntity<?> deleteShootingPacket(@RequestParam String uuid, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName(), UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return shootingPacketService.deleteShootingPacket(uuid, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PostMapping("/updateShootingPacket")
    public ResponseEntity<?> updateShootingPacket(@RequestParam String uuid, @RequestParam String name, @RequestParam Float price, @RequestBody Map<String, Integer> map, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName(), UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return shootingPacketService.updateShootingPacket(uuid, name, price, map, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PutMapping("/addAmmo")
    public ResponseEntity<?> updateAmmoQuantity(@RequestParam String caliberUUID, @RequestParam Integer count, @RequestParam String date, @RequestParam String time, @RequestParam String description, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            LocalDate parse = LocalDate.parse(date);
            LocalTime parseTime = LocalTime.parse(time);
            String imageUUID = filesService.storeImageAddedAmmo(imageString, pinCode);
            return armoryService.updateAmmo(caliberUUID, count, parse, parseTime, description, imageUUID, pinCode);
        }
        return code;
    }

    @Transactional
    @PutMapping("/signUpkeepAmmo")
    public ResponseEntity<?> signUpkeepAmmo(@RequestParam String ammoInEvidenceUUID, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            String imageUUID = filesService.storeImageUpkeepAmmo(imageString, pinCode);
            return armoryService.signUpkeepAmmo(ammoInEvidenceUUID, imageUUID, pinCode);
        }
        return code;
    }

    @Transactional
    @PostMapping("/addGun")
    public ResponseEntity<?> addGunEntity(@RequestBody AddGunImageWrapper addGunImageWrapper, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            String imageUUID = filesService.storeImageAddGun(addGunImageWrapper.getImageString(), pinCode);
            return armoryService.addGunEntity(addGunImageWrapper, imageUUID, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PostMapping("/signAddGun")
    public ResponseEntity<?> signAddGun(@RequestParam String gunUUID, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            String imageUUID = filesService.storeImageAddGun(imageString, pinCode);
            return armoryService.addGunSign(gunUUID, imageUUID, pinCode);
        } else {
            return code;
        }

    }

    @Transactional
    @PostMapping("/editGun")
    public ResponseEntity<?> editGunEntity(@RequestBody Gun gun) {
        return armoryService.editGunEntity(gun);
    }

    @Transactional
    @PutMapping("/remove")
    public ResponseEntity<?> removeGun(@RequestParam String gunUUID, @RequestParam String pinCode,@RequestParam String basisOfRemoved, @RequestBody String imageString) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            String imageUUID = filesService.storeImageRemoveGun(imageString, pinCode);
            return armoryService.removeGun(gunUUID,basisOfRemoved, pinCode, imageUUID);
        } else {
            return code;
        }
    }

    @Transactional
    @PostMapping("/calibers")
    public ResponseEntity<?> createNewCaliber(@RequestParam String caliber, @RequestParam String pinCode) throws NoUserPermissionException {
        if (caliber.isEmpty()) {
            return ResponseEntity.badRequest().body("Wprowadź dane");
        }
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return caliberService.createNewCaliber(caliber, pinCode);
        } else {
            return code;
        }
    }
    @Transactional
    @PostMapping("/activateOrDeactivateCaliber")
    public ResponseEntity<?> activateOrDeactivateCaliber(@RequestParam String caliberUUID, @RequestParam String pinCode) throws NoUserPermissionException {
        if (caliberUUID.isEmpty()) {
            return ResponseEntity.badRequest().body("Wprowadź dane");
        }
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return caliberService.activateOrDeactivateCaliber(caliberUUID, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/changeCaliberUnitPrice")
    public ResponseEntity<?> changeCaliberUnitPrice(@RequestParam String caliberUUID, @RequestParam Float price, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName(), UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return armoryService.changeCaliberUnitPrice(caliberUUID, price, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PatchMapping("/changeCaliberUnitPriceForNotMember")
    public ResponseEntity<?> changeCaliberUnitPriceForNotMember(@RequestParam String caliberUUID, @RequestParam Float price, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName(), UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return armoryService.changeCaliberUnitPriceForNotMember(caliberUUID, price, pinCode);
        } else {
            return code;
        }
    }

    @Transactional
    @PostMapping("/newGunTypeName")
    public ResponseEntity<?> createNewGunStore(@RequestParam String nameType) {
        if (nameType.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return armoryService.createNewGunStore(nameType);
    }

    @Transactional
    @PostMapping("/addGunToList")
    public ResponseEntity<?> addGunToList(@RequestBody List<String> gunUUID, @RequestParam String date, @RequestParam String time) {
        LocalDate parseDate = LocalDate.parse(date);
        LocalTime parseTime = LocalTime.parse(time);
        return armoryService.addGunToList(gunUUID, parseDate, parseTime);
    }

    @Transactional
    @PutMapping("/signIssuanceGun")
    public ResponseEntity<?> signIssuanceGun(@RequestParam String gunUsedUUID, @RequestParam String issuanceDate, @RequestParam String issuanceTime, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        LocalDate parseDate = LocalDate.parse(issuanceDate);
        LocalTime parseTime = LocalTime.parse(issuanceTime);
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            String imageUUID = filesService.storeImageIssuanceGun(imageString, pinCode);
            return armoryService.signIssuanceGun(gunUsedUUID, imageUUID, parseDate, parseTime, pinCode);
        }
        return code;
    }

    @Transactional
    @PutMapping("/signTakerGun")
    public ResponseEntity<?> signTakerGun(@RequestParam String gunUsedUUID, @RequestParam Integer memberLeg, @RequestBody String imageString) {
        String imageUUID = filesService.storeImageTakerGun(imageString, memberLeg);
        return armoryService.signTakerGun(gunUsedUUID, imageUUID, memberLeg);
    }

    @Transactional
    @PutMapping("/signReturnerGun")
    public ResponseEntity<?> signReturnerGun(@RequestParam String gunUsedUUID, @RequestParam Integer memberLeg, @RequestBody String imageString) {
        String imageUUID = filesService.storeImageReturnerGun(imageString, memberLeg);
        return armoryService.signReturnerGun(gunUsedUUID, imageUUID, memberLeg);
    }

    @Transactional
    @PutMapping("/signAcceptanceGun")
    public ResponseEntity<?> signAcceptanceGun(@RequestParam String gunUsedUUID, @RequestParam String acceptanceDate, @RequestParam String acceptanceTime, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        List<String> acceptedPermissions = List.of(UserSubType.WEAPONS_WAREHOUSEMAN.getName());
        LocalDate parseDate = LocalDate.parse(acceptanceDate);
        LocalTime parseTime = LocalTime.parse(acceptanceTime);
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode, acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            String imageUUID = filesService.storeImageIssuanceGun(imageString, pinCode);
            return armoryService.signAcceptanceGun(gunUsedUUID, imageUUID, parseDate, parseTime, pinCode);
        }
        return code;
    }


}
