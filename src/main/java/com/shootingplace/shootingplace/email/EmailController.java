package com.shootingplace.shootingplace.email;

import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.security.RequirePermissions;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/email")
@CrossOrigin
public class EmailController {
    private final EmailService emailService;
    private final ChangeHistoryService changeHistoryService;

    public EmailController(EmailService emailService, ChangeHistoryService changeHistoryService) {
        this.emailService = emailService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping
    public ResponseEntity<?> getConnections() {
        return emailService.getAllConnections();
    }

    @GetMapping("/scheduled")
    public ResponseEntity<?> getScheduledEmails() {
        return emailService.getScheduledEmails();
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentEmails(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parsedFirstDate = LocalDate.parse(firstDate);
        LocalDate parsedSecondDate = LocalDate.parse(secondDate);

        return emailService.getSentEmails(parsedFirstDate, parsedSecondDate);
    }

    @GetMapping("/mailingConfigList")
    public ResponseEntity<?> getMailingConfigList() {
        return ResponseEntity.ok(emailService.getMailingConfigList());
    }

    @PostMapping("/mailingConfigList")
    public ResponseEntity<?> setMailingConfigList(@RequestBody Map<String, Boolean> map) {
        return ResponseEntity.ok(emailService.setMailingConfigList(map));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteScheduledMail(@RequestParam String uuid) {
        return emailService.deleteScheduledMail(uuid);
    }

    @Transactional
    @PostMapping("/save")
    public ResponseEntity<?> saveScheduledEmail(@RequestBody EmailRequest request, @RequestParam String uuid) {
        return emailService.saveScheduledEmail(request, uuid);
    }

    @Transactional
    @PostMapping("/sendEmails")
    public ResponseEntity<?> sendEmails(@RequestBody EmailRequest request, @RequestParam List<String> emailList) {
        return emailService.sendCustomEmails(request, emailList);
    }

    @Transactional
    @PostMapping("/sendSingleEmail")
    public ResponseEntity<?> sendSingleEmail(@RequestBody EmailRequest request) {
        System.out.println("wchodzę");
        return emailService.sendSingleEmail(request);
    }

    @Transactional
    @PostMapping("/manualRemindersForNonActive")
    public ResponseEntity<?> sendManualRemindersForNonActive() {
        return emailService.sendRemindersForNonActive();
    }

    @Transactional
    @PostMapping("/manualRemindersForActive")
    public ResponseEntity<?> sendManualRemindersForActive() {
        return emailService.sendRemindersForActiveOneMonthBefore();
    }

    @Transactional
    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail(@RequestBody EmailRequest request) throws MessagingException {
        return emailService.sendTestEmail(request);
    }

    @Transactional
    @PutMapping
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.ADMIN, UserSubType.SUPER_USER})
    public ResponseEntity<?> saveConnection(@RequestBody EmailConfig emailConfig, @RequestParam String pinCode) {
        return emailService.saveConnection(emailConfig);
    }

    @PutMapping("/edit")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER, UserSubType.ADMIN, UserSubType.SUPER_USER})
    public ResponseEntity<?> editConnection(@RequestBody EmailConfig emailConfig, @RequestParam String pinCode, @RequestParam String uuid) throws NoUserPermissionException {
        return emailService.editConnection(emailConfig, uuid);
    }

}
