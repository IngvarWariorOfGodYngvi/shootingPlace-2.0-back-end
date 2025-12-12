package com.shootingplace.shootingplace.license;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.member.Member;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class LicenseService {

    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final HistoryService historyService;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public LicenseService(MemberRepository memberRepository,
                          LicenseRepository licenseRepository, HistoryService historyService, LicensePaymentHistoryRepository licensePaymentHistoryRepository) {
        this.memberRepository = memberRepository;
        this.licenseRepository = licenseRepository;
        this.historyService = historyService;
        this.licensePaymentHistoryRepository = licensePaymentHistoryRepository;
    }

    public List<MemberDTO> getMembersNamesAndLicense() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue().stream()
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl()))
                .collect(Collectors.toList());
    }

    private static Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    public List<MemberDTO> getMembersNamesAndLicenseNotValid() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidFalse().stream()
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl()))
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> updateLicense(String memberUUID, License license) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        LicenseEntity licenseEntity = memberEntity.getLicense();
        if (memberEntity.getShootingPatent().getPatentNumber() == null && memberEntity.isAdult()) {
            LOG.info("Brak Patentu");
            return ResponseEntity.badRequest().body("Brak Patentu");
        }
        if (licenseEntity.getNumber() != null) {
            boolean match = memberRepository.findAllByErasedFalse()
                    .stream()
                    .filter(f -> f.getLicense().getNumber() != null)
                    .anyMatch(f -> f.getLicense().getNumber().equals(license.getNumber()));
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
            if (memberEntity.getHistory().getLicensePaymentHistory().size() > 0) {
                Integer validForYear = memberEntity.getHistory().getLicensePaymentHistory().get(0).getValidForYear();
                licenseEntity.setValidThru(LocalDate.of(validForYear, 12, 31));
                licenseEntity.setValid(true);
                LOG.info("Brak ręcznego ustawienia daty, ustawiono na koniec bieżącego roku " + licenseEntity.getValidThru());
            }
        }
        licenseEntity.setNumber(license.getNumber());
        licenseEntity.setPaid(false);
        licenseRepository.save(licenseEntity);
        LOG.info("Zaktualizowano licencję");
        return ResponseEntity.ok("Zaktualizowano licencję");
    }

    public ResponseEntity<?> updateLicense(String memberUUID, String number, LocalDate date, Boolean isPaid, Boolean pistol, Boolean rifle, Boolean shotgun, String pinCode) throws NoUserPermissionException {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        LicenseEntity licenseEntity = memberEntity.getLicense();

        if (number != null && !number.isEmpty() && !number.equals("null")) {

            boolean match = memberRepository.findAllByErasedFalse()
                    .stream()
                    .filter(f -> f.getLicense().getNumber() != null)
                    .anyMatch(f -> f.getLicense().getNumber().equals(number));

            if (match && !licenseEntity.getNumber().equals(number)) {
                LOG.error("Ktoś już ma taki numer licencji");
                return ResponseEntity.badRequest().body("Ktoś już ma taki numer licencji");
            } else {
                licenseEntity.setNumber(number);
                LOG.info("Dodano numer licencji");
            }

        }
        if (date != null) {
            licenseEntity.setValidThru(date);
            licenseEntity.setValid(licenseEntity.getValidThru().getYear() >= LocalDate.now().getYear());
        }
        if (isPaid != null && !isPaid.equals("null")) {
            licenseEntity.setPaid(isPaid);
        }
        if (pistol != null) {
            licenseEntity.setPistolPermission(pistol);
        }
        if (rifle != null) {
            licenseEntity.setRiflePermission(rifle);
        }
        if (shotgun != null) {
            licenseEntity.setShotgunPermission(shotgun);
        }
        ResponseEntity<?> response = historyService.getStringResponseEntityLicense(pinCode, licenseEntity, HttpStatus.OK, "updateLicense", "Poprawiono Licencję");

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            licenseRepository.save(licenseEntity);
        }
        return response;

    }

    public ResponseEntity<?> prolongLicenseValid(String memberUUID, License license) {

        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        LicenseEntity licenseEntity = memberEntity.getLicense();


        if (licenseEntity.getNumber() != null && licenseEntity.isPaid()) {
            if (LocalDate.now().isAfter(LocalDate.of(licenseEntity.getValidThru().getYear(), 11, 1)) || licenseEntity.getValidThru().isBefore(LocalDate.now())) {
                licenseEntity.setValidThru(LocalDate.of((licenseEntity.getValidThru().getYear() + 1), 12, 31));
                licenseEntity.setValid(licenseEntity.getValidThru().getYear() >= LocalDate.now().getYear());
                if (license.getPistolPermission() != null) {
                    if (!memberEntity.getShootingPatent().getPistolPermission() && memberEntity.isAdult()) {
                        LOG.error("Brak Patentu - Pistolet");
                    }
                    if (license.getPistolPermission() != null && memberEntity.getShootingPatent().getPistolPermission()) {
                        if (!license.getPistolPermission()) {
                            historyService.addLicenseHistoryRecord(memberUUID, 0);
                        }
                        licenseEntity.setPistolPermission(license.getPistolPermission());
                        LOG.info("Dodano dyscyplinę : pistolet");
                    }
                }
                if (license.getRiflePermission() != null) {
                    if (!memberEntity.getShootingPatent().getRiflePermission() && memberEntity.isAdult()) {
                        LOG.error("Brak Patentu - Karabin");
                    }
                    if (license.getRiflePermission() != null && memberEntity.getShootingPatent().getRiflePermission()) {
                        if (!license.getRiflePermission()) {
                            historyService.addLicenseHistoryRecord(memberUUID, 1);
                        }
                        licenseEntity.setRiflePermission(license.getRiflePermission());
                        LOG.info("Dodano dyscyplinę : karabin");
                    }
                }
                if (license.getShotgunPermission() != null) {
                    if (!memberEntity.getShootingPatent().getShotgunPermission() && memberEntity.isAdult()) {
                        LOG.error("Brak Patentu - Strzelba");
                    }
                    if (license.getShotgunPermission() != null && memberEntity.getShootingPatent().getShotgunPermission()) {
                        if (!license.getShotgunPermission()) {
                            historyService.addLicenseHistoryRecord(memberUUID, 2);
                        }
                        licenseEntity.setShotgunPermission(license.getShotgunPermission());
                        LOG.info("Dodano dyscyplinę : strzelba");
                    }
                }
                licenseEntity.setCanProlong(false);
                licenseEntity.setPaid(false);
                historyService.checkStarts(memberUUID);
                licenseRepository.save(licenseEntity);
                LOG.info("Przedłużono licencję");
                return ResponseEntity.ok().body("Przedłużono licencję");

            } else {
                LOG.error("Nie można przedłużyć licencji - należy poczekać do 1 listopada");
                return ResponseEntity.status(403).body("Nie można przedłużyć licencji - należy poczekać do 1 listopada");
            }
        } else {
            LOG.error("Nie można przedłużyć licencji");
            return ResponseEntity.badRequest().body("Nie można przedłużyć licencji");
        }
    }

    public License getLicense() {
        return License.builder()
                .number(null)
                .validThru(null)
                .pistolPermission(false)
                .riflePermission(false)
                .shotgunPermission(false)
                .isValid(false)
                .canProlong(false)
                .isPaid(false)
                .build();
    }

    public ResponseEntity<?> prolongAllLicense(List<String> licenseList, String pinCode) throws NoUserPermissionException {
        List<String> responseList = new ArrayList<>();
        String s1 = "";

        for (String s : licenseList) {
            MemberEntity memberEntity = memberRepository.findById(s).orElse(null);
            LicenseEntity licenseEntity;

            if (memberEntity == null) {
                LOG.info("Nie znaleziono osoby o podanym ID");
                responseList.add("Nie znaleziono osoby");
            } else {
                if (memberEntity.getLicense() != null) {
                    licenseEntity = memberEntity.getLicense();
                    License license = Mapping.map(licenseEntity);
                    ResponseEntity<?> responseEntity = prolongLicenseValid(s, license);
                        String s2 = memberEntity.getFullName() + ": " + responseEntity.getBody()+";";
                        responseList.add(s2);

                } else {
                    LOG.info(memberEntity.getSecondName() + " Nie posiada Licencji");
                    responseList.add(memberEntity.getSecondName() + " Nie posiada licencji");
                }
                s1 = s1.concat(s);

            }
        }
        return historyService.getStringResponseEntityLicense(pinCode, null, HttpStatus.OK, "prolongAllLicense", responseList);

    }

    public List<?> getAllLicencePayment() {

        List<LicensePaymentHistoryDTO> list1 = new ArrayList<>();
        licensePaymentHistoryRepository.findAllByPayInPZSSPortalFalse().forEach(e -> {
            MemberEntity member = memberRepository.getOne(e.getMemberUUID());
            list1.add(LicensePaymentHistoryDTO.builder()
                    .paymentUuid(e.getUuid())
                    .firstName(member.getFirstName())
                    .secondName(member.getSecondName())
                    .email(member.getEmail())
                    .active(member.isActive())
                    .adult(member.isAdult())
                    .legitimationNumber(member.getLegitimationNumber())
                    .memberUUID(member.getUuid())
                    .isPayInPZSSPortal(e.isPayInPZSSPortal())
                    .date(e.getDate())
                    .licenseUUID(e.getUuid())
                    .validForYear(e.getValidForYear())
                    .isNew(e.isNew())
                    .build());
        });
        return list1.stream()
                .sorted(Comparator.comparing(LicensePaymentHistoryDTO::getSecondName, pl()).thenComparing(LicensePaymentHistoryDTO::getFirstName, pl())).collect(Collectors.toList());
    }

    public LicenseEntity getLicense(String LicenseUUID) {
        return licenseRepository.getOne(LicenseUUID);
    }

    public List<LicensePaymentHistoryEntity> getLicensePaymentHistory(String MemberUUID) {
        return memberRepository.getOne(MemberUUID).getHistory().getLicensePaymentHistory();
    }

    public List<?> allNoLicenseWithPayment() {

        return memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> f.getLicense().getNumber() == null && f.getHistory()
                        .getLicensePaymentHistory().size() > 0)
                .map(Mapping::map)
                .sorted(Comparator.comparing(Member::getSecondName, pl()).thenComparing(Member::getFirstName, pl()))
                .collect(Collectors.toList());
    }

    public List<MemberDTO> allLicensesQualifyingToProlong() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue().stream()
                .filter(f ->
                        (f.getHistory().getPistolCounter() >= 4 && f.getHistory().getRifleCounter() >= 2 && f.getHistory().getShotgunCounter() >= 2) ||
                                (f.getHistory().getPistolCounter() >= 2 && f.getHistory().getRifleCounter() >= 4 && f.getHistory().getShotgunCounter() >= 2) ||
                                (f.getHistory().getPistolCounter() >= 2 && f.getHistory().getRifleCounter() >= 2 && f.getHistory().getShotgunCounter() >= 4)
                )
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl()))
                .collect(Collectors.toList());
    }

    public List<MemberDTO> allLicensesNotQualifyingToProlong() {
        return memberRepository.findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue().stream()
                .filter(f -> !f.getLicense().isPaid())
                .filter(f ->
                        (f.getHistory().getPistolCounter() < 4 && f.getHistory().getRifleCounter() < 2 && f.getHistory().getShotgunCounter() < 2) ||
                                (f.getHistory().getPistolCounter() < 2 && f.getHistory().getRifleCounter() < 4 && f.getHistory().getShotgunCounter() < 2) ||
                                (f.getHistory().getPistolCounter() < 2 && f.getHistory().getRifleCounter() < 2 && f.getHistory().getShotgunCounter() < 4)
                )
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl()))
                .collect(Collectors.toList());
    }
}