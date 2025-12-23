package com.shootingplace.shootingplace.contributions;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.security.RequirePermissions;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/contribution")
@CrossOrigin
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;

    @Transactional
    @PatchMapping("/{memberUUID}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> addContribution(@PathVariable String memberUUID, @RequestParam String pinCode, @RequestParam Integer contributionCount) {
        return contributionService.addContribution(memberUUID, LocalDate.now(), contributionCount);
    }

    @Transactional
    @PutMapping("/edit")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> editContribution(@RequestParam String memberUUID, @RequestParam String contributionUUID, @RequestParam String paymentDay, @RequestParam String validThru, @RequestParam String pinCode) {
        LocalDate parsedPaymentDay = null;
        if (paymentDay != null && !paymentDay.isEmpty() && !paymentDay.equals("null")) {
            parsedPaymentDay = LocalDate.parse(paymentDay);
        }
        LocalDate parsedValidThru = null;
        if (validThru != null && !validThru.isEmpty() && !validThru.equals("null")) {
            parsedValidThru = LocalDate.parse(validThru);
        }
        return contributionService.updateContribution(memberUUID, contributionUUID, parsedPaymentDay, parsedValidThru);
    }

    @Transactional
    @PatchMapping("/remove/{memberUUID}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> removeContribution(@PathVariable String memberUUID, @RequestParam String contributionUUID, @RequestParam String pinCode) {
        return contributionService.removeContribution(memberUUID, contributionUUID, pinCode);
    }
}
