package com.shootingplace.shootingplace.license;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryEntity;
import com.shootingplace.shootingplace.security.RequirePermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/license")
@CrossOrigin
public class LicenseController {

    private final ChangeHistoryService changeHistoryService;
    private final LicenseService licenseService;
    private final LicensePaymentService licensePaymentService;

    public LicenseController(ChangeHistoryService changeHistoryService, LicenseService licenseService, LicensePaymentService licensePaymentService) {
        this.changeHistoryService = changeHistoryService;
        this.licenseService = licenseService;
        this.licensePaymentService = licensePaymentService;
    }

    @GetMapping("/getLicense")
    public LicenseEntity getLicense(@RequestParam String licenseUUID) {
        return licenseService.getLicense(licenseUUID);
    }

    @GetMapping("/getLicensePaymentHistory")
    public List<LicensePaymentHistoryEntity> getLicensePaymentHistory(@RequestParam String memberUUID) {
        return licenseService.getLicensePaymentHistory(memberUUID);
    }

    @GetMapping("/membersWithValidLicense")
    public ResponseEntity<List<?>> getMembersNamesAndLicense() {
        return ResponseEntity.ok(licenseService.getMembersNamesAndLicense());
    }

    @GetMapping("/membersWithNotValidLicense")
    public ResponseEntity<List<?>> getMembersNamesAndLicenseNotValid() {
        return ResponseEntity.ok(licenseService.getMembersNamesAndLicenseNotValid());
    }

    @GetMapping("/allLicencePayment")
    public ResponseEntity<?> getAllLicensePayment() {
        return ResponseEntity.ok(licenseService.getAllLicencePayment());
    }

    @GetMapping("/allNoLicenseWithPayment")
    public ResponseEntity<?> allNoLicenseWithPayment() {
        return ResponseEntity.ok(licenseService.allNoLicenseWithPayment());
    }

    @GetMapping("/LicensesQualifyingToProlong")
    public ResponseEntity<?> getLicensesQualifyingToProlong() {
        return ResponseEntity.ok(licenseService.allLicensesQualifyingToProlong());
    }

    @GetMapping("/LicensesNotQualifyingToProlong")
    public ResponseEntity<?> LicensesNotQualifyingToProlong() {
        return ResponseEntity.ok(licenseService.allLicensesNotQualifyingToProlong());
    }

    @Transactional
    @PutMapping("/{memberUUID}")
    public ResponseEntity<?> updateLicense(@PathVariable String memberUUID, @RequestBody License license) {
        return licenseService.updateLicense(memberUUID, license);
    }

    @Transactional
    @PutMapping("/forceUpdate")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> updateLicense(@RequestParam String memberUUID, @RequestParam String number, @RequestParam String date, @RequestParam String pinCode, @Nullable @RequestParam String isPaid, @Nullable @RequestParam Boolean pistol, @Nullable @RequestParam Boolean rifle, @Nullable @RequestParam Boolean shotgun) throws NoUserPermissionException {
        String parseNumber = (number != null && !number.isEmpty() && !number.equals("null")) ? number : null;
        LocalDate parseDate = (date != null && !date.isEmpty() && !date.equals("null")) ? LocalDate.parse(date) : null;
        Boolean parseIsPaid = (isPaid != null && !isPaid.isEmpty() && !isPaid.equals("null")) ? Boolean.valueOf(isPaid) : null;
        return parseNumber == null && parseDate == null && parseIsPaid == null && pistol == null && rifle == null && shotgun == null ? ResponseEntity.badRequest().body("Należy podać co najmniej jedną zmienną") : licenseService.updateLicense(memberUUID, parseNumber, parseDate, parseIsPaid, pistol, rifle, shotgun, pinCode);
    }

    @Transactional
    @PatchMapping("/{memberUUID}")
    public ResponseEntity<?> renewLicenseValidDate(@PathVariable String memberUUID, @RequestBody License license) {
        return licenseService.prolongLicenseValid(memberUUID, license);
    }

    @Transactional
    @PatchMapping("/prolongAll")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> prolongAllLicenseWherePaidInPZSSIsTrue(@RequestParam List<String> licenseList, @RequestParam String pinCode) throws NoUserPermissionException {
        return licenseService.prolongAllLicense(licenseList, pinCode);

    }

    @Transactional
    @PutMapping("/history/{memberUUID}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> addLicensePaymentHistory(@PathVariable String memberUUID, @RequestParam String pinCode) throws NoUserPermissionException {
        return licensePaymentService.addLicenseHistoryPayment(memberUUID, pinCode);
    }

    @Transactional
    @PatchMapping("/paymentChange")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> paymentChange(@RequestParam String paymentUUID, @RequestParam String pinCode, @RequestParam boolean condition) throws NoUserPermissionException {
        return licensePaymentService.toggleLicencePaymentInPZSS(paymentUUID, condition, pinCode);
    }

    @Transactional
    @PatchMapping("/paymentToggleArray")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> toggleLicencePaymentInPZSSArray(@RequestParam List<String> paymentUUIDs, @RequestParam String pinCode, @RequestParam boolean condition) throws NoUserPermissionException {
        ResponseEntity<?> result = null;
        for (String paymentUUID : paymentUUIDs) {
            result = licensePaymentService.toggleLicencePaymentInPZSS(paymentUUID, condition, pinCode);
        }
        return result;
    }

    @Transactional
    @PutMapping("/editPayment")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> editLicensePaymentHistory(@RequestParam String memberUUID, @RequestParam String paymentUUID, @RequestParam String paymentDate, @RequestParam Integer year, @RequestParam String pinCode) throws NoUserPermissionException {
        LocalDate parseDate = null;
        if (paymentDate != null && !paymentDate.isEmpty() && !paymentDate.equals("null")) {
            parseDate = LocalDate.parse(paymentDate);
        }
        return licensePaymentService.updateLicensePayment(memberUUID, paymentUUID, parseDate, year, pinCode);
    }

    @Transactional
    @DeleteMapping("/removePayment")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> removeLicensePaymentRecord(@RequestParam String paymentUUID, @RequestParam String pinCode) throws NoUserPermissionException {
            return licensePaymentService.removeLicensePaymentRecord(paymentUUID, pinCode);
    }

}
