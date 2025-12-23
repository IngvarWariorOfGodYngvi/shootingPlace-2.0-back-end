package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.file.FilesService;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/armory")
@CrossOrigin
@RequiredArgsConstructor
public class ArmoryController {

    private final ArmoryService armoryService;
    private final CaliberService caliberService;
    private final AmmoUsedService ammoUsedService;
    private final ShootingPacketService shootingPacketService;
    private final FilesService filesService;

    @GetMapping("/recount")
    public void recount() {
        ammoUsedService.recountAmmo();
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
        return ResponseEntity.ok(armoryService.getAllGunUsed(parseFirstDate, parseSecondDate));
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
    public ResponseEntity<?> getCalibersQuantity(@RequestParam String uuid, @RequestParam String date) {
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
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> addShootingPacket(@RequestParam String name, @RequestParam float price, @RequestBody Map<String, Integer> map, @RequestParam String pinCode) {
        return shootingPacketService.addShootingPacket(name, price, map);
    }

    @Transactional
    @DeleteMapping("/deleteShootingPacket")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> deleteShootingPacket(@RequestParam String uuid, @RequestParam String pinCode) {
        return shootingPacketService.deleteShootingPacket(uuid);
    }

    @Transactional
    @PostMapping("/updateShootingPacket")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> updateShootingPacket(@RequestParam String uuid, @RequestParam String name, @RequestParam Float price, @RequestBody Map<String, Integer> map, @RequestParam String pinCode) {
        return shootingPacketService.updateShootingPacket(uuid, name, price, map);
    }

    @Transactional
    @PutMapping("/addAmmo")
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> updateAmmoQuantity(@RequestParam String caliberUUID, @RequestParam Integer count, @RequestParam String date, @RequestParam String time, @RequestParam String description, @RequestParam String pinCode, @RequestBody String imageString) {
        LocalDate parse = LocalDate.parse(date);
        LocalTime parseTime = LocalTime.parse(time);
        String imageUUID = filesService.storeImageAddedAmmo(imageString, pinCode);
        return armoryService.updateAmmo(caliberUUID, count, parse, parseTime, description, imageUUID, pinCode);
    }

    @Transactional
    @PutMapping("/signUpkeepAmmo")
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> signUpkeepAmmo(@RequestParam String ammoInEvidenceUUID, @RequestParam String pinCode, @RequestBody String imageString) {
        String imageUUID = filesService.storeImageUpkeepAmmo(imageString, pinCode);
        return armoryService.signUpkeepAmmo(ammoInEvidenceUUID, imageUUID, pinCode);
    }

    @Transactional
    @PostMapping("/addGun")
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> addGunEntity(@RequestBody AddGunImageWrapper addGunImageWrapper, @RequestParam String pinCode) {
        String imageUUID = filesService.storeImageAddGun(addGunImageWrapper.getImageString(), pinCode);
        return armoryService.addGunEntity(addGunImageWrapper, imageUUID, pinCode);
    }

    @Transactional
    @PostMapping("/signAddGun")
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> signAddGun(@RequestParam String gunUUID, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        String imageUUID = filesService.storeImageAddGun(imageString, pinCode);
        return armoryService.addGunSign(gunUUID, imageUUID, pinCode);
    }

    @Transactional
    @PostMapping("/editGun")
    public ResponseEntity<?> editGunEntity(@RequestBody Gun gun) {
        return armoryService.editGunEntity(gun);
    }

    @Transactional
    @PutMapping("/remove")
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> removeGun(@RequestParam String gunUUID, @RequestParam String pinCode, @RequestParam String basisOfRemoved, @RequestBody String imageString) {
        String imageUUID = filesService.storeImageRemoveGun(imageString, pinCode);
        return armoryService.removeGun(gunUUID, basisOfRemoved, pinCode, imageUUID);
    }

    @Transactional
    @PostMapping("/calibers")
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> createNewCaliber(@RequestParam String caliber, @RequestParam String pinCode) {
        if (caliber.isEmpty()) {
            return ResponseEntity.badRequest().body("Wprowadź dane");
        }
        return caliberService.createNewCaliber(caliber, pinCode);
    }

    @Transactional
    @PostMapping("/activateOrDeactivateCaliber")
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> activateOrDeactivateCaliber(@RequestParam String caliberUUID, @RequestParam String pinCode) {
        if (caliberUUID.isEmpty()) {
            return ResponseEntity.badRequest().body("Wprowadź dane");
        }
        return caliberService.activateOrDeactivateCaliber(caliberUUID);
    }

    @Transactional
    @PatchMapping("/changeCaliberUnitPrice")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> changeCaliberUnitPrice(@RequestParam String caliberUUID, @RequestParam Float price, @RequestParam String pinCode) {
        return armoryService.changeCaliberUnitPrice(caliberUUID, price);
    }

    @Transactional
    @PatchMapping("/changeCaliberUnitPriceForNotMember")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> changeCaliberUnitPriceForNotMember(@RequestParam String caliberUUID, @RequestParam Float price, @RequestParam String pinCode) {
        return armoryService.changeCaliberUnitPriceForNotMember(caliberUUID, price);
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
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> signIssuanceGun(@RequestParam String gunUsedUUID, @RequestParam String issuanceDate, @RequestParam String issuanceTime, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        LocalDate parseDate = LocalDate.parse(issuanceDate);
        LocalTime parseTime = LocalTime.parse(issuanceTime);
        String imageUUID = filesService.storeImageIssuanceGun(imageString, pinCode);
        return armoryService.signIssuanceGun(gunUsedUUID, imageUUID, parseDate, parseTime, pinCode);
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
    @RequirePermissions(value = {UserSubType.WEAPONS_WAREHOUSEMAN})
    public ResponseEntity<?> signAcceptanceGun(@RequestParam String gunUsedUUID, @RequestParam String acceptanceDate, @RequestParam String acceptanceTime, @RequestParam String pinCode, @RequestBody String imageString) throws NoUserPermissionException {
        LocalDate parseDate = LocalDate.parse(acceptanceDate);
        LocalTime parseTime = LocalTime.parse(acceptanceTime);
        String imageUUID = filesService.storeImageIssuanceGun(imageString, pinCode);
        return armoryService.signAcceptanceGun(gunUsedUUID, imageUUID, parseDate, parseTime, pinCode);
    }


}
