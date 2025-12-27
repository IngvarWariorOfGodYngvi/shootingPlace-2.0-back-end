package com.shootingplace.shootingplace.license;

import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.history.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.member.Member;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final HistoryService historyService;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;
    private final Logger LOG = LogManager.getLogger(getClass());

    public List<MemberDTO> getMembersNamesAndLicense() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue().stream().map(Mapping::map2DTO).sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl())).collect(Collectors.toList());
    }

    private static Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    public List<MemberDTO> getMembersNamesAndLicenseNotValid() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidFalse().stream().map(Mapping::map2DTO).sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl())).collect(Collectors.toList());
    }

    public ResponseEntity<?> updateLicense(String memberUUID, License license) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        LicenseEntity licenseEntity = memberEntity.getLicense();
        if (memberEntity.getShootingPatent().getPatentNumber() == null && memberEntity.isAdult()) {
            LOG.info("Brak Patentu");
            return ResponseEntity.badRequest().body("Brak Patentu");
        }
        if (licenseEntity.getNumber() != null) {
            boolean match = memberRepository.findAllByErasedFalse().stream().filter(f -> f.getLicense().getNumber() != null).anyMatch(f -> f.getLicense().getNumber().equals(license.getNumber()));
            if (match && !licenseEntity.getNumber().equals(license.getNumber())) {
                LOG.error("Ktoś już ma taki numer licencji");
                return ResponseEntity.badRequest().body("Ktoś już ma taki numer licencji");
            } else {
                licenseEntity.setNumber(license.getNumber());
                LOG.info("Dodano numer licencji");
            }
        }
        if (license.getPistolPermission() != null) {
            if (license.getPistolPermission()) {
                historyService.addLicenseHistoryRecord(memberUUID, 0);
                licenseEntity.setPistolPermission(license.getPistolPermission());
                LOG.info("Dodano dyscyplinę : pistolet");
            }
        }
        if (license.getRiflePermission() != null) {
            if (license.getRiflePermission()) {
                historyService.addLicenseHistoryRecord(memberUUID, 1);
                licenseEntity.setRiflePermission(license.getRiflePermission());
                LOG.info("Dodano dyscyplinę : karabin");
            }
        }
        if (license.getShotgunPermission() != null) {
            if (license.getShotgunPermission()) {
                historyService.addLicenseHistoryRecord(memberUUID, 2);
                licenseEntity.setShotgunPermission(license.getShotgunPermission());
                LOG.info("Dodano dyscyplinę : strzelba");
            }
        }
        if (license.getValidThru() != null) {
            licenseEntity.setValidThru(LocalDate.of(license.getValidThru().getYear(), 12, 31));
            if (license.getValidThru().getYear() >= LocalDate.now().getYear()) {
                licenseEntity.setValid(true);
            }
            LOG.info("zaktualizowano datę licencji");
        } else {
            if (!memberEntity.getHistory().getLicensePaymentHistory().isEmpty()) {
                Integer validForYear = memberEntity.getHistory().getLicensePaymentHistory().getFirst().getValidForYear();
                licenseEntity.setValidThru(LocalDate.of(validForYear, 12, 31));
                licenseEntity.setValid(true);
                LOG.info("Brak ręcznego ustawienia daty, ustawiono na koniec bieżącego roku {}", licenseEntity.getValidThru());
            }
        }
        licenseEntity.setNumber(license.getNumber());
        licenseEntity.setPaid(false);
        licenseRepository.save(licenseEntity);
        LOG.info("Zaktualizowano licencję");
        return ResponseEntity.ok("Zaktualizowano licencję");
    }

    @RecordHistory(action = "License.prolongAll", entity = HistoryEntityType.LICENSE, entityArgIndex = 0)
    public ResponseEntity<?> updateLicense(String memberUUID, String number, LocalDate date, Boolean isPaid, Boolean pistol, Boolean rifle, Boolean shotgun) {

        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));

        LicenseEntity license = member.getLicense();

        if (number != null && !number.isBlank()) {
            boolean exists = memberRepository.findAllByErasedFalse().stream().map(m -> m.getLicense().getNumber()).filter(Objects::nonNull).anyMatch(number::equals);

            if (exists && !number.equals(license.getNumber())) {
                return ResponseEntity.badRequest().body("Ktoś już ma taki numer licencji");
            }
            license.setNumber(number);
        }

        if (date != null) {
            license.setValidThru(date);
            license.setValid(date.getYear() >= LocalDate.now().getYear());
        }

        if (isPaid != null) license.setPaid(isPaid);
        if (pistol != null) license.setPistolPermission(pistol);
        if (rifle != null) license.setRiflePermission(rifle);
        if (shotgun != null) license.setShotgunPermission(shotgun);

        licenseRepository.save(license);

        return ResponseEntity.ok("Poprawiono licencję");
    }


    public ResponseEntity<?> prolongLicenseValid(String memberUUID, License license) {

        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));

        LicenseEntity licenseEntity = member.getLicense();

        if (licenseEntity.getNumber() == null || !licenseEntity.isPaid()) {
            LOG.error("Nie można przedłużyć licencji");
            return ResponseEntity.badRequest().body("Nie można przedłużyć licencji");
        }

        LocalDate now = LocalDate.now();
        LocalDate validThru = licenseEntity.getValidThru();
        LocalDate earliestProlongDate = LocalDate.of(validThru.getYear(), 11, 1);

        if (now.isBefore(earliestProlongDate) && !validThru.isBefore(now)) {
            LOG.error("Nie można przedłużyć licencji - należy poczekać do 1 listopada");
            return ResponseEntity.status(403).body("Nie można przedłużyć licencji - należy poczekać do 1 listopada");
        }

        int newYear = validThru.getYear() + 1;
        licenseEntity.setValidThru(LocalDate.of(newYear, 12, 31));
        licenseEntity.setValid(newYear >= now.getYear());

        handlePermission(license.getPistolPermission(), member.getShootingPatent().isPistolPermission(), member.isAdult(), () -> licenseEntity.setPistolPermission(license.getPistolPermission()), () -> historyService.addLicenseHistoryRecord(memberUUID, 0), "pistolet");

        handlePermission(license.getRiflePermission(), member.getShootingPatent().isRiflePermission(), member.isAdult(), () -> licenseEntity.setRiflePermission(license.getRiflePermission()), () -> historyService.addLicenseHistoryRecord(memberUUID, 1), "karabin");

        handlePermission(license.getShotgunPermission(), member.getShootingPatent().isShotgunPermission(), member.isAdult(), () -> licenseEntity.setShotgunPermission(license.getShotgunPermission()), () -> historyService.addLicenseHistoryRecord(memberUUID, 2), "strzelba");

        licenseEntity.setCanProlong(false);
        licenseEntity.setPaid(false);

        historyService.checkStarts(memberUUID);
        licenseRepository.save(licenseEntity);

        LOG.info("Przedłużono licencję");
        return ResponseEntity.ok("Przedłużono licencję");
    }

    private void handlePermission(Boolean requested, boolean hasPatent, boolean adult, Runnable applyPermission, Runnable addHistory, String name) {
        if (requested == null) {
            return;
        }
        if (!hasPatent && adult) {
            LOG.error("Brak Patentu - {}", name);
            return;
        }
        if (!requested) {
            addHistory.run();
        }
        applyPermission.run();
        LOG.info("Dodano dyscyplinę : {}", name);
    }


    public License getLicense() {
        return License.builder().number(null).validThru(null).pistolPermission(false).riflePermission(false).shotgunPermission(false).isValid(false).canProlong(false).isPaid(false).build();
    }

    @RecordHistory(action = "License.prolongList", entity = HistoryEntityType.LICENSE)
    public ResponseEntity<?> prolongAllLicense(List<String> licenseList) {
        List<String> responseList = new ArrayList<>();
        for (String memberUuid : licenseList) {
            MemberEntity member = memberRepository.findById(memberUuid).orElse(null);
            if (member == null) {
                responseList.add("Nie znaleziono osoby");
                continue;
            }
            LicenseEntity license = member.getLicense();
            if (license == null) {
                responseList.add(member.getFullName() + ": brak licencji");
                continue;
            }
            try {
                prolongLicenseValid(memberUuid, Mapping.map(license));
                responseList.add(member.getFullName() + ": przedłużono licencję");
            } catch (Exception e) {
                responseList.add(member.getFullName() + ": błąd przedłużania");
            }
        }
        return ResponseEntity.ok(responseList);
    }


    public List<?> getAllLicencePayment() {

        List<LicensePaymentHistoryDTO> list1 = new ArrayList<>();
        licensePaymentHistoryRepository.findAllByPayInPZSSPortalFalse().forEach(e -> {
            MemberEntity memberEntity = memberRepository.findById(e.getMemberUUID()).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Klubowicza"));
            list1.add(LicensePaymentHistoryDTO.builder().paymentUuid(e.getUuid()).firstName(memberEntity.getFirstName()).secondName(memberEntity.getSecondName()).email(memberEntity.getEmail()).active(memberEntity.isActive()).adult(memberEntity.isAdult()).legitimationNumber(memberEntity.getLegitimationNumber()).memberUUID(memberEntity.getUuid()).isPayInPZSSPortal(e.isPayInPZSSPortal()).date(e.getDate()).licenseUUID(e.getUuid()).validForYear(e.getValidForYear()).isNew(e.isNew()).build());
        });
        return list1.stream().sorted(Comparator.comparing(LicensePaymentHistoryDTO::getSecondName, pl()).thenComparing(LicensePaymentHistoryDTO::getFirstName, pl())).collect(Collectors.toList());
    }

    public LicenseEntity getLicense(String LicenseUUID) {
        return licenseRepository.findById(LicenseUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Licencji"));
    }

    public List<LicensePaymentHistoryEntity> getLicensePaymentHistory(String MemberUUID) {
        return memberRepository.findById(MemberUUID).orElseThrow(EntityNotFoundException::new).getHistory().getLicensePaymentHistory();
    }

    public List<?> allNoLicenseWithPayment() {

        return memberRepository.findAllByErasedFalse().stream().filter(f -> f.getLicense().getNumber() == null && !f.getHistory().getLicensePaymentHistory().isEmpty()).map(Mapping::map).sorted(Comparator.comparing(Member::getSecondName, pl()).thenComparing(Member::getFirstName, pl())).collect(Collectors.toList());
    }

    public List<MemberDTO> allLicensesQualifyingToProlong() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue().stream().filter(f -> (f.getHistory().getPistolCounter() >= 4 && f.getHistory().getRifleCounter() >= 2 && f.getHistory().getShotgunCounter() >= 2) || (f.getHistory().getPistolCounter() >= 2 && f.getHistory().getRifleCounter() >= 4 && f.getHistory().getShotgunCounter() >= 2) || (f.getHistory().getPistolCounter() >= 2 && f.getHistory().getRifleCounter() >= 2 && f.getHistory().getShotgunCounter() >= 4)).map(Mapping::map2DTO).sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl())).collect(Collectors.toList());
    }

    public List<MemberDTO> allLicensesNotQualifyingToProlong() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue().stream().filter(f -> !f.getLicense().isPaid()).filter(f -> (f.getHistory().getPistolCounter() < 4 && f.getHistory().getRifleCounter() < 2 && f.getHistory().getShotgunCounter() < 2) || (f.getHistory().getPistolCounter() < 2 && f.getHistory().getRifleCounter() < 4 && f.getHistory().getShotgunCounter() < 2) || (f.getHistory().getPistolCounter() < 2 && f.getHistory().getRifleCounter() < 2 && f.getHistory().getShotgunCounter() < 4)).map(Mapping::map2DTO).sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl())).collect(Collectors.toList());
    }
}