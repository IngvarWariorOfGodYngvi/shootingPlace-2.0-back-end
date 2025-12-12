package com.shootingplace.shootingplace.license;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.users.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LicensePaymentService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final LicenseRepository licenseRepository;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;
    private final HistoryRepository historyRepository;
    private final HistoryService historyService;

    private final EmailService emailService;

    private final Logger LOG = LogManager.getLogger(getClass());


    public LicensePaymentService(MemberRepository memberRepository, UserRepository userRepository, LicenseRepository licenseRepository, LicensePaymentHistoryRepository licensePaymentHistoryRepository, HistoryRepository historyRepository, HistoryService historyService, EmailService emailService) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.licenseRepository = licenseRepository;
        this.licensePaymentHistoryRepository = licensePaymentHistoryRepository;
        this.historyRepository = historyRepository;
        this.historyService = historyService;
        this.emailService = emailService;
    }

    public ResponseEntity<?> addLicenseHistoryPayment(String memberUUID, String pinCode) throws NoUserPermissionException {

        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        LicenseEntity licenseEntity = memberEntity.getLicense();

        HistoryEntity historyEntity = memberEntity.getHistory();
        if (!licenseEntity.isPaid()) {
            if (historyEntity.getLicensePaymentHistory() == null) {
                historyEntity.setLicensePaymentHistory(new ArrayList<>());
            }
            int dateYear = memberEntity.getLicense().getValidThru() != null ? memberEntity.getLicense().getValidThru().getYear() + 1 : LocalDate.now().getYear();
            List<LicensePaymentHistoryEntity> licensePaymentHistory = historyEntity.getLicensePaymentHistory();
            String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
            LicensePaymentHistoryEntity build = LicensePaymentHistoryEntity.builder()
                    .date(LocalDate.now())
                    .validForYear(dateYear)
                    .memberUUID(memberUUID)
                    .isPayInPZSSPortal(false)
                    .isNew(licenseEntity.getNumber() == null)
                    .acceptedBy(userRepository.findByPinCode(pin).orElseThrow(EntityNotFoundException::new).getFullName())
                    .build();
            licensePaymentHistoryRepository.save(build);
            licensePaymentHistory.add(build);

            LOG.info("Dodano wpis o nowej płatności za licencję " + LocalDate.now());
            historyRepository.save(historyEntity);
            emailService.sendLicensePaymentConfirmation(memberUUID);

        } else {
            return ResponseEntity.badRequest().body("Licencja na ten moment jest opłacona");
        }

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "addLicenseHistoryPayment", "Dodano płatność za Licencję");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            licenseEntity.setPaid(true);
            licenseRepository.save(licenseEntity);
        }
        return response;


    }
    public ResponseEntity<?> toggleLicencePaymentInPZSS(String paymentUUID, boolean condition, String pinCode) throws NoUserPermissionException {

        if (!licensePaymentHistoryRepository.existsById(paymentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono płatności");
        }

        LicensePaymentHistoryEntity licensePaymentHistoryEntity = licensePaymentHistoryRepository.findById(paymentUUID).orElseThrow(EntityNotFoundException::new);
        licensePaymentHistoryEntity.setPayInPZSSPortal(condition);
        licensePaymentHistoryRepository.save(licensePaymentHistoryEntity);
        return historyService.getStringResponseEntity(pinCode, memberRepository.getOne(licensePaymentHistoryEntity.getMemberUUID()), HttpStatus.OK, "toggleLicencePaymentInPZSS", "Oznaczono jako " + (condition ? "" : "nie") + "opłacone w Portalu PZSS");
    }
    public ResponseEntity<?> updateLicensePayment(String memberUUID, String paymentUUID, LocalDate date, Integer year, String pinCode) throws NoUserPermissionException {

        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        LicensePaymentHistoryEntity licensePaymentHistoryEntity = memberEntity.getHistory().getLicensePaymentHistory().stream().filter(f -> f.getUuid().equals(paymentUUID)).findFirst().orElseThrow(EntityNotFoundException::new);

        if (date != null) {
            licensePaymentHistoryEntity.setDate(date);
        }
        if (year != null) {
            licensePaymentHistoryEntity.setValidForYear(year);
        }
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "updateLicensePayment", "Poprawiono płatność za licencję");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            licensePaymentHistoryRepository.save(licensePaymentHistoryEntity);
        }
        return response;
    }
    public ResponseEntity<?> removeLicensePaymentRecord(String paymentUUID, String pinCode) throws NoUserPermissionException {

        LicensePaymentHistoryEntity licensePaymentHistoryEntity = licensePaymentHistoryRepository.findById(paymentUUID).orElseThrow(EntityNotFoundException::new);

        MemberEntity memberEntity = memberRepository.findById(licensePaymentHistoryEntity.getMemberUUID()).orElseThrow(EntityNotFoundException::new);

        HistoryEntity history = memberEntity.getHistory();

        history.getLicensePaymentHistory().remove(licensePaymentHistoryEntity);

        historyRepository.save(history);
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "removeLicensePaymentRecord", "usunięto");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            licensePaymentHistoryRepository.delete(licensePaymentHistoryEntity);
        }
        return response;
    }
}
