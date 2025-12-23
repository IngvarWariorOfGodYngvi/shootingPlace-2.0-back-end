package com.shootingplace.shootingplace.license;

import com.shootingplace.shootingplace.email.EmailService;
import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LicensePaymentService {

    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;
    private final HistoryRepository historyRepository;

    private final EmailService emailService;

    private final Logger LOG = LogManager.getLogger(getClass());

    @Transactional
    @RecordHistory(
            action = "License.addPayment",
            entity = HistoryEntityType.LICENSE_PAYMENT,
            entityArgIndex = 0
    )
    public ResponseEntity<?> addLicenseHistoryPayment(String memberUUID) {
        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        LicenseEntity license = member.getLicense();
        if (license == null) {
            return ResponseEntity.badRequest().body("Klubowicz nie posiada licencji");
        }
        if (license.isPaid()) {
            return ResponseEntity.badRequest().body("Licencja na ten moment jest opłacona");
        }
        HistoryEntity history = member.getHistory();
        if (history.getLicensePaymentHistory() == null) {
            history.setLicensePaymentHistory(new ArrayList<>());
        }
        int validForYear = license.getValidThru() != null
                ? license.getValidThru().getYear() + 1
                : LocalDate.now().getYear();
        LicensePaymentHistoryEntity payment = LicensePaymentHistoryEntity.builder()
                .date(LocalDate.now())
                .validForYear(validForYear)
                .memberUUID(memberUUID)
                .isPayInPZSSPortal(false)
                .isNew(license.getNumber() == null)
                .build();
        licensePaymentHistoryRepository.save(payment);
        history.getLicensePaymentHistory().add(payment);
        historyRepository.save(history);
        license.setPaid(true);
        licenseRepository.save(license);
        emailService.sendLicensePaymentConfirmation(memberUUID);
        LOG.info("Dodano wpis o nowej płatności za licencję {}", LocalDate.now());
        return ResponseEntity.ok("Dodano płatność za Licencję");
    }


    @Transactional
    @RecordHistory(action = "License.togglePaymentInPZSS", entity = HistoryEntityType.LICENSE_PAYMENT, entityArgIndex = 0   // paymentUUID
    )
    public ResponseEntity<?> toggleLicencePaymentInPZSS(String paymentUUID, boolean condition) {
        LicensePaymentHistoryEntity payment = licensePaymentHistoryRepository.findById(paymentUUID).orElse(null);
        if (payment == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono płatności");
        }
        payment.setPayInPZSSPortal(condition);
        licensePaymentHistoryRepository.save(payment);
        LOG.info("Zmieniono status PZSS paymentUUID={} na {}", paymentUUID, condition);
        return ResponseEntity.ok("Oznaczono jako " + (condition ? "" : "nie ") + "opłacone w Portalu PZSS");
    }


    @Transactional
    @RecordHistory(action = "LicensePayment.update", entity = HistoryEntityType.LICENSE_PAYMENT, entityArgIndex = 1)
    public ResponseEntity<?> updateLicensePayment(String memberUUID, String paymentUUID, LocalDate date, Integer year) {
        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);

        if (member == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono klubowicza");
        }
        LicensePaymentHistoryEntity payment = member.getHistory().getLicensePaymentHistory().stream().filter(p -> p.getUuid().equals(paymentUUID)).findFirst().orElse(null);
        if (payment == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono płatności");
        }
        if (date != null) {
            payment.setDate(date);
        }
        if (year != null) {
            payment.setValidForYear(year);
        }
        licensePaymentHistoryRepository.save(payment);
        LOG.info("Zaktualizowano płatność licencji paymentUUID={}", paymentUUID);
        return ResponseEntity.ok("Poprawiono płatność za licencję");
    }


    @Transactional
    @RecordHistory(action = "LicensePayment.remove", entity = HistoryEntityType.LICENSE_PAYMENT, entityArgIndex = 0)
    public ResponseEntity<?> removeLicensePaymentRecord(String paymentUUID) {
        LicensePaymentHistoryEntity payment = licensePaymentHistoryRepository.findById(paymentUUID).orElse(null);
        if (payment == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono płatności");
        }
        MemberEntity member = memberRepository.findById(payment.getMemberUUID()).orElse(null);
        if (member == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono klubowicza");
        }
        HistoryEntity history = member.getHistory();
        history.getLicensePaymentHistory().remove(payment);
        historyRepository.save(history);
        licensePaymentHistoryRepository.delete(payment);
        LOG.info("Usunięto płatność licencji paymentUUID={}", paymentUUID);
        return ResponseEntity.ok("Usunięto płatność licencji");
    }


}
