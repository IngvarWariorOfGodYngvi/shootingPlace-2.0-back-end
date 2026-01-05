package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.ammoEvidence.*;
import com.shootingplace.shootingplace.exceptions.NoPersonToAmmunitionException;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.member.PersonalEvidenceEntity;
import com.shootingplace.shootingplace.member.PersonalEvidenceRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmmoUsedService {
    private final PersonalEvidenceRepository personalEvidenceRepository;
    private final AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository;
    private final AmmoInEvidenceService ammoInEvidenceService;
    private final AmmoUsedRepository ammoUsedRepository;
    private final CaliberRepository caliberRepository;
    private final CaliberService caliberService;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final ArmoryService armoryService;
    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;

    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;

    private final Logger LOG = LogManager.getLogger();

    public boolean isEvidenceIsClosedOrEqual(int quantity) {
        return ammoEvidenceRepository.existsByOpenTrueAndForceOpenFalse() ?
                ammoEvidenceRepository.findAllByOpenTrue()
                        .stream()
                        .findFirst()
                        .orElseThrow(EntityNotFoundException::new)
                        .getAmmoInEvidenceEntityList()
                        .stream()
                        .mapToInt(AmmoInEvidenceEntity::getQuantity)
                        .sum() == quantity : ammoEvidenceRepository.existsByOpenTrueAndForceOpenFalse();
    }

    @Transactional
    public ResponseEntity<String> addAmmoUsedEntity(String caliberUUID, Integer legitimationNumber, Integer otherID, Integer quantity) throws NoPersonToAmmunitionException {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }
        if (ammoEvidenceRepository.existsByOpenTrueAndForceOpenFalse()) {
            List<AmmoEvidenceEntity> collect = new ArrayList<>(ammoEvidenceRepository.findAllByOpenTrueAndForceOpenFalse());
            if (collect.size() > 1) {
                collect.forEach(e -> {
                    e.setForceOpen(false);
                    e.setOpen(false);
                    LOG.info("Zamykam listę {}", e.getNumber());
                    ammoEvidenceRepository.save(e);
                });
                return ResponseEntity.badRequest().body("Wystąpił błąd, ponów próbę za chwilę");
            }

            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findAllByOpenTrueAndForceOpenFalse().stream().findFirst().orElseThrow(EntityNotFoundException::new);
            if (ammoEvidenceEntity.getDate().isBefore(LocalDate.now())) {
                ammoEvidenceEntity.setOpen(false);
                ammoEvidenceEntity.setForceOpen(false);
                ammoEvidenceRepository.save(ammoEvidenceEntity);
                LOG.info("zamknięto starą listę");
            }
            AmmoInEvidenceEntity ammoInEvidenceEntity = ammoEvidenceEntity.getAmmoInEvidenceEntityList().stream().filter(f -> f.getCaliberUUID().equals(caliberUUID)).findFirst().orElse(null);

            if (ammoInEvidenceEntity != null && ammoInEvidenceEntity.isLocked()) {
                return ResponseEntity.badRequest().body("Nie można dodać do listy - Kaliber zastał zatwierdzony i zablokowany");
            }
        }
        CaliberEntity one = caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new);
        boolean substrat = quantity > 0;
        if (substrat) {
            armoryService.substratAmmo(caliberUUID, quantity);
            LOG.info("dodaję amunicję do listy");
        } else {
            LOG.info("odejmuję amunicję z listy");
        }
        MemberEntity memberEntity = null;
        OtherPersonEntity otherPersonEntity = null;
        if (legitimationNumber > 0) {
            memberEntity = memberRepository.findByLegitimationNumber(legitimationNumber).orElseThrow(EntityNotFoundException::new);
            LOG.info("member {}", memberEntity.getFullName());
            AmmoUsedPersonal ammoUsedPersonal = AmmoUsedPersonal.builder()
                    .caliberName(one.getName())
                    .counter(quantity)
                    .memberUUID(memberEntity.getUuid())
                    .caliberUUID(caliberUUID)
                    .memberName(memberEntity.getFullName())
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .build();
            validateAmmo(ammoUsedPersonal);
        } else {
            if (otherID != null) {
                otherPersonEntity = otherPersonRepository
                        .findById(otherID).orElseThrow(EntityNotFoundException::new);
                LOG.info("not member {}", otherPersonEntity.getFullName());
            } else {
                throw new NoPersonToAmmunitionException();
            }
        }
        AmmoUsedEvidence ammoUsedEvidence = AmmoUsedEvidence.builder()
                .caliberName(one.getName())
                .counter(quantity)
                .memberEntity(memberEntity)
                .otherPersonEntity(otherPersonEntity)
                .userName(memberEntity != null ? memberEntity.getFullName() : otherPersonEntity.getFullName())
                .caliberUUID(caliberUUID)
                .date(LocalDate.now())
                .time(LocalTime.now())
                .build();
        if (starEvidence(ammoUsedEvidence)) {
            return ResponseEntity.ok((substrat ? "Dodano do listy " : "Zwrócono do magazynu ") + (memberEntity != null ? memberEntity.getFullName() : otherPersonEntity.getFullName()) + " " + one.getName() + " " + quantity);

        }
        return ResponseEntity.badRequest().body("Coś poszło nie tak - Sprawdź stany magazynowe " + one.getName());
    }

    @Transactional
    public ResponseEntity<?> addListOfAmmoToEvidence(Map<String, String> caliberUUIDAmmoQuantityMap, Integer legitimationNumber, Integer otherID) throws NoPersonToAmmunitionException {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }

        if (ammoEvidenceRepository.existsByOpenTrueAndForceOpenFalse()) {
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findAllByOpenTrueAndForceOpenFalse().stream().findFirst().orElseThrow(EntityNotFoundException::new);
            if (ammoEvidenceEntity.getDate().isBefore(LocalDate.now())) {
                ammoEvidenceEntity.setOpen(false);
                ammoEvidenceEntity.setForceOpen(false);
                ammoEvidenceRepository.save(ammoEvidenceEntity);
                LOG.info("zamknięto starą listę");
            }
        }
        boolean[] caliberAmmoCheck = new boolean[caliberUUIDAmmoQuantityMap.size()];
        final int[] iterator = {0};
        caliberUUIDAmmoQuantityMap.forEach((key, value) -> {
            caliberAmmoCheck[iterator[0]] = caliberService.getCaliberAmmoInStore(key) - Integer.parseInt(value) >= 0;
            iterator[0]++;
        });
        boolean check = true;
        for (boolean b : caliberAmmoCheck) {
            if (!b) {
                check = false;
                break;
            }
        }
        if (!check) {
            return ResponseEntity.badRequest().body("Coś poszło nie tak - Sprawdź stany magazynowe ");
        }
        List<String> returnList = new ArrayList<>();
        for (Map.Entry<String, String> entry : caliberUUIDAmmoQuantityMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            CaliberEntity one = caliberRepository.findById(key).orElseThrow(EntityNotFoundException::new);
            boolean substrat = Integer.parseInt(value) > 0;
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findAllByOpenTrue()
                    .stream().findFirst().orElse(null);
            if (ammoEvidenceEntity != null) {
                AmmoInEvidenceEntity ammoInEvidenceEntity = ammoEvidenceEntity.getAmmoInEvidenceEntityList()
                        .stream()
                        .filter(f -> f.getCaliberUUID().equals(one.getUuid()))
                        .findFirst()
                        .orElse(null);
                if (ammoInEvidenceEntity != null && ammoInEvidenceEntity.isLocked()) {
                    returnList.add("Nie można dodać amunicji bo lista została zablokowana");
                }
            }
            if (substrat) {
                armoryService.substratAmmo(key, Integer.parseInt(value));
                LOG.info("dodaję amunicję do listy");
            } else {
                LOG.info("odejmuję amunicję z listy");
            }
            MemberEntity memberEntity = null;
            OtherPersonEntity otherPersonEntity = null;
            if (legitimationNumber > 0) {
                memberEntity = memberRepository.findByLegitimationNumber(legitimationNumber).orElseThrow(EntityNotFoundException::new);
                LOG.info("member {}", memberEntity.getFullName());
                AmmoUsedPersonal ammoUsedPersonal = AmmoUsedPersonal.builder()
                        .caliberName(one.getName())
                        .counter(Integer.parseInt(value))
                        .memberUUID(memberEntity.getUuid())
                        .caliberUUID(key)
                        .memberName(memberEntity.getFullName())
                        .date(LocalDate.now())
                        .time(LocalTime.now())
                        .build();
                validateAmmo(ammoUsedPersonal);
            } else {
                if (otherID != null) {
                    otherPersonEntity = otherPersonRepository
                            .findById(otherID).orElseThrow(EntityNotFoundException::new);
                    LOG.info("not member {}", otherPersonEntity.getFullName());
                } else {
                    throw new NoPersonToAmmunitionException();
                }
            }
            AmmoUsedEvidence ammoUsedEvidence = AmmoUsedEvidence.builder()
                    .caliberName(one.getName())
                    .counter(Integer.parseInt(value))
                    .memberEntity(memberEntity)
                    .otherPersonEntity(otherPersonEntity)
                    .userName(memberEntity != null ? memberEntity.getFullName() : otherPersonEntity.getFullName())
                    .caliberUUID(key)
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .build();
            if (starEvidence(ammoUsedEvidence)) {
                returnList.add((substrat ? "Dodano do listy " : "Zwrócono do magazynu ") + (memberEntity != null ? memberEntity.getFullName() : otherPersonEntity.getFullName()) + " " + one.getName() + " " + value);
            }


        }
        return ResponseEntity.ok(returnList);
    }

    private void validateAmmo(AmmoUsedPersonal ammoUsedpersonal) {
        PersonalEvidenceEntity personalEvidence = memberRepository
                .findById(ammoUsedpersonal.getMemberUUID()).orElseThrow(EntityNotFoundException::new)
                .getPersonalEvidence();

        boolean match = personalEvidence
                .getAmmoList()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(e -> e.getCaliberUUID()
                        .equals(ammoUsedpersonal.getCaliberUUID()) &&
                        e.getCaliberName()
                                .equals(ammoUsedpersonal.getCaliberName()));
        if (!match) {
            AmmoUsedEntity ammoUsedEntity = createAmmoUsedEntity(ammoUsedpersonal);
            if (ammoUsedEntity.getCounter() < 0) {
                ammoUsedEntity.setCounter(0);
            }
            ammoUsedRepository.save(ammoUsedEntity);
            personalEvidence.getAmmoList().add(ammoUsedEntity);
            personalEvidence.getAmmoList().sort(Comparator.comparing(AmmoUsedEntity::getCaliberName));
            personalEvidenceRepository.save(personalEvidence);
        } else {
            AmmoUsedEntity ammoUsedEntity = personalEvidence
                    .getAmmoList()
                    .stream()
                    .filter(e -> e.getCaliberUUID().equals(ammoUsedpersonal.getCaliberUUID()))
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);

            Integer counter = (ammoUsedEntity.getCounter() != null ? ammoUsedEntity.getCounter() : 0);
            ammoUsedEntity.setCounter(counter + ammoUsedpersonal.getCounter());
            if (ammoUsedEntity.getCounter() < 0) {
                ammoUsedEntity.setCounter(0);
            }

            ammoUsedRepository.save(ammoUsedEntity);
        }

    }

    private boolean starEvidence(AmmoUsedEvidence ammoUsedEvidence) {
        return ammoInEvidenceService.addAmmoUsedEntityToAmmoInEvidenceEntity(ammoUsedToEvidenceEntityRepository.save(createAmmoUsedToEvidenceEntity(ammoUsedEvidence)));
    }

    private AmmoUsedEntity createAmmoUsedEntity(AmmoUsedPersonal ammoUsedPersonal) {
        return ammoUsedRepository.save(Mapping.map(ammoUsedPersonal));
    }

    private AmmoUsedToEvidenceEntity createAmmoUsedToEvidenceEntity(AmmoUsedEvidence ammoUsedEvidence) {
        return ammoUsedToEvidenceEntityRepository.save(Mapping.map(ammoUsedEvidence));
    }

    public void recountAmmo() {
        LOG.info("Przeliczam amunicję");
        List<AmmoUsedToEvidenceEntity> all2 = ammoUsedToEvidenceEntityRepository.findAll();
        List<AmmoInEvidenceEntity> all1 = ammoInEvidenceRepository.findAll();
        all1.forEach(e -> e.getAmmoUsedToEvidenceEntityList().forEach(all2::remove));
        List<AmmoUsedToEvidenceEntity> all3 = ammoUsedToEvidenceEntityRepository.findAll();
        all3.removeAll(all2);
        Set<String> set1 = new HashSet<>();
        all3.forEach(e -> {
            if (e.getMemberEntity() != null) {
                set1.add(e.getMemberEntity().getUuid());
            }
        });
        set1.forEach(e -> {
            MemberEntity id = memberRepository.findById(e).orElseThrow(EntityNotFoundException::new);
            List<AmmoUsedToEvidenceEntity> collect = all3.stream().filter(f -> f.getMemberEntity() != null).filter(f -> f.getMemberEntity().getUuid().equals(e)).toList();
            Map<String, Integer> map =
                    collect
                            .stream()
                            .collect(Collectors.groupingBy(AmmoUsedToEvidenceEntity::getCaliberName, Collectors.summingInt(AmmoUsedToEvidenceEntity::getCounter)));
            id.getPersonalEvidence().getAmmoList().forEach(f -> {
                f.setCounter(map.get(f.getCaliberName()));
                ammoUsedRepository.save(f);
            });
        });
        ammoUsedToEvidenceEntityRepository.deleteAll(all2);
    }

    public List<AmmoUsedToEvidenceDTO> getPersonalAmmoFromList(String legitimationNumber, String idNumber, String evidenceID) {

        if (idNumber == null || idNumber.equals("null")) {
            idNumber = null;
        }
        if (legitimationNumber.equals("null")) {
            legitimationNumber = null;
        }
        AmmoEvidenceEntity one = ammoEvidenceRepository.findById(evidenceID).orElseThrow(EntityNotFoundException::new);
        List<AmmoUsedToEvidenceDTO> collect = new ArrayList<>();
        if (legitimationNumber != null) {
            String finalLegitimationNumber = legitimationNumber;
            one.getAmmoInEvidenceEntityList().stream().map(Mapping::map)
                    .forEach(e -> collect.addAll(e.getAmmoUsedToEvidenceDTOList()
                            .stream()
                            .filter(f -> f.getLegitimationNumber() != null && f.getLegitimationNumber().equals(Integer.valueOf(finalLegitimationNumber)))
                            .toList()));
        }
        if (idNumber != null) {
            String finalIdNumber = idNumber;
            one.getAmmoInEvidenceEntityList().stream().map(Mapping::map)
                    .forEach(e -> collect.addAll(e.getAmmoUsedToEvidenceDTOList()
                            .stream()
                            .filter(f -> f.getIDNumber() != null && f.getIDNumber().equals(Integer.valueOf(finalIdNumber)))
                            .toList()));
        }
        collect.forEach(e-> {
            e.setUnitPriceForNonMember(caliberRepository.findCaliberByName(e.getCaliberName()).getUnitPriceForNotMember());
            e.setUnitPrice(caliberRepository.findCaliberByName(e.getCaliberName()).getUnitPrice());
        });
        return collect;
    }
}
