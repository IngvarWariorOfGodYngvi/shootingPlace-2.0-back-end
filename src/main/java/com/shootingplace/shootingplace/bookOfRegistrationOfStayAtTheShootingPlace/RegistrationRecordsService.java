package com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import com.shootingplace.shootingplace.wrappers.ImageOtherPersonWrapper;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RegistrationRecordsService {

    private final Logger LOG = LogManager.getLogger(getClass());

    private final MemberRepository memberRepo;
    private final RegistrationRecordRepository registrationRepo;
    private final OtherPersonRepository otherPersonRepository;
    private final WorkingTimeEvidenceService workingTimeEvidenceService;
    private final UserRepository userRepository;

    public RegistrationRecordsService(MemberRepository memberRepo, RegistrationRecordRepository registrationRepo, OtherPersonRepository otherPersonRepository, WorkingTimeEvidenceService workingTimeEvidenceService, UserRepository userRepository) {
        this.memberRepo = memberRepo;
        this.registrationRepo = registrationRepo;
        this.otherPersonRepository = otherPersonRepository;
        this.workingTimeEvidenceService = workingTimeEvidenceService;
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> createRecordInBook(String pesel, String imageUUID) {
        RegistrationRecordEntity r = new RegistrationRecordEntity();
        MemberEntity member = memberRepo.findAllByErasedFalse().stream().filter(f->f.getPesel().equals(pesel)).findFirst().orElse(null);
        if (member != null && !member.isErased()) {
            if (registrationRepo.findAll().stream().anyMatch(f ->
                    LocalDate.of(f.getDateTime().getYear(), f.getDateTime().getMonth(), f.getDateTime().getDayOfMonth()).equals(LocalDate.now()) && f.getPeselOrID().equals(member.getPesel()))) {
                LOG.info("Osoba znajduje się już na liście");
                return ResponseEntity.badRequest().body("Osoba znajduje się już na liście");
            } else {
                // jeśli klubowicz jest również użytokwnikiem to włączam mu czas pracy
                UserEntity userEntity = userRepository.findByMemberUuid(member.getUuid()).orElseThrow(EntityNotFoundException::new);
                if (userEntity != null) {
                    workingTimeEvidenceService.openWTEByUser(userEntity);
                }
                r.setFirstName(member.getFirstName());
                r.setSecondName(member.getSecondName());
                r.setDataProcessingAgreement(true);
                r.setStatementOnReadingTheShootingPlaceRegulations(true);

                if (member.getWeaponPermission().getNumber() == null) {
                    r.setAddress(member.getAddress().toString());
                } else {
                    r.setWeaponPermission(member.getWeaponPermission().getNumber());
                }
                r.setDateTime(LocalDateTime.now());
                r.setImageUUID(imageUUID);
                r.setPeselOrID(member.getPesel());
                r.setDayIndex(getDayIndex() + 1);
                String name = r.getSecondName() + ' ' + r.getFirstName();
                LOG.info("Zapisano do książki " + name);
                registrationRepo.save(r);
                return ResponseEntity.ok("Zapisano do książki " + name);
            }
        } else {
            return ResponseEntity.badRequest().body("Brak osoby w bazie");
        }
    }

    public ResponseEntity<?> createRecordInBook(String imageUUID, OtherPersonEntity otherPersonEntity) {
        RegistrationRecordEntity r = new RegistrationRecordEntity();
        if (registrationRepo.findAll().stream().anyMatch(f ->
                LocalDate.of(f.getDateTime().getYear(), f.getDateTime().getMonth(), f.getDateTime().getDayOfMonth()).equals(LocalDate.now()) && f.getPeselOrID().equals(String.valueOf(otherPersonEntity.getId())))) {
            LOG.info("Osoba znajduje się już na liście");
            return ResponseEntity.badRequest().body("Osoba znajduje się już na liście");
        } else {
            r.setFirstName(firstLetterToUpperCase(otherPersonEntity.getFirstName()));
            r.setSecondName(otherPersonEntity.getSecondName().toUpperCase(Locale.ROOT));
            r.setDataProcessingAgreement(true);
            r.setStatementOnReadingTheShootingPlaceRegulations(true);
            if (otherPersonEntity.getWeaponPermissionNumber() == null) {
                r.setAddress(otherPersonEntity.getAddress().toString());
            } else {
                r.setWeaponPermission(otherPersonEntity.getWeaponPermissionNumber());
            }
            r.setDateTime(LocalDateTime.now());
            r.setImageUUID(imageUUID);
            r.setPeselOrID(String.valueOf(otherPersonEntity.getId()));
            r.setDayIndex(getDayIndex() + 1);
            String name = r.getSecondName() + ' ' + r.getFirstName();
            LOG.info("Zapisano do książki " + name);
            registrationRepo.save(r);
            return ResponseEntity.ok("Zapisano do książki " + name);
        }
    }

    public ResponseEntity<?> createRecordInBook(String imageUUID, ImageOtherPersonWrapper other) {
        RegistrationRecordEntity r = new RegistrationRecordEntity();
        if (other.getOther().getId() != null) {
            OtherPersonEntity one = otherPersonRepository.getOne(Integer.valueOf(other.getOther().getId()));
            if (registrationRepo.findAll().stream().anyMatch(f ->
                    LocalDate.of(f.getDateTime().getYear(), f.getDateTime().getMonth(), f.getDateTime().getDayOfMonth()).equals(LocalDate.now()) && f.getPeselOrID().equals(String.valueOf(one.getId())))) {
                LOG.info("Osoba znajduje się już na liście");
                return ResponseEntity.badRequest().body("Osoba znajduje się już na liście");
            }
            r.setFirstName(firstLetterToUpperCase(one.getFirstName()));
            r.setSecondName(one.getSecondName().toUpperCase(Locale.ROOT));
            r.setDataProcessingAgreement(true);
            r.setStatementOnReadingTheShootingPlaceRegulations(true);
            r.setPeselOrID(String.valueOf(one.getId()));
            if (one.getWeaponPermissionNumber() == null) {
                r.setAddress(one.getAddress().toString());
            } else {
                r.setWeaponPermission(one.getWeaponPermissionNumber());
            }
        } else {
            r.setFirstName(firstLetterToUpperCase(other.getOther().getFirstName()));
            r.setSecondName(other.getOther().getSecondName().toUpperCase(Locale.ROOT));
            r.setDataProcessingAgreement(true);
            r.setStatementOnReadingTheShootingPlaceRegulations(true);
            r.setPeselOrID("");
            if (other.getOther().getWeaponPermissionNumber() == null) {
                r.setAddress(other.getOther().getAddress().toString());
            } else {
                r.setWeaponPermission(other.getOther().getWeaponPermissionNumber());
            }
        }
        r.setDateTime(LocalDateTime.now());
        r.setImageUUID(imageUUID);

        r.setDayIndex(getDayIndex() + 1);
        String name = r.getSecondName() + ' ' + r.getFirstName();
        LOG.info("Zapisano do książki " + name);
        registrationRepo.save(r);
        return ResponseEntity.ok("Zapisano do książki " + name);

    }

    private int getDayIndex() {
        RegistrationRecordEntity registrationRecordEntity = registrationRepo.findAll()
                .stream()
                .filter(f -> f.getDateTime().equals(LocalDateTime.now()))
                .max(Comparator.comparing(RegistrationRecordEntity::getDayIndex))
                .orElse(null);
        if (registrationRecordEntity != null) {
            return registrationRecordEntity.getDayIndex();
        }
        return 0;
    }

    private String firstLetterToUpperCase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public ResponseEntity<?> getRecordsBetweenDate(LocalDate firstDate, LocalDate secondDate) {
        return ResponseEntity.ok(registrationRepo
                .findAllBeetweenDate(LocalDateTime.of(firstDate, LocalTime.of(0, 0)), LocalDateTime.of(secondDate, LocalTime.of(23, 59, 59)))
                .stream().sorted(Comparator.comparing(RegistrationRecordEntity::getDateTime).reversed()));

    }

    public void setEndTimeToAllRegistrationRecordEntity() {
        List<RegistrationRecordEntity> collect = registrationRepo.findAllByEndDateTimeNull();
        collect.forEach(e -> e.setEndDateTime(LocalDateTime.of(e.getDateTime().toLocalDate(), LocalTime.of(23, 59, 59))));
        LOG.info("Ustawiono datę wyjścia wszystkim na godzinę 23:59:59");
    }
}
