package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.ammoEvidence.*;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AmmoInEvidenceService {
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository;
    private final ArmoryService armoryService;

    private final AmmoEvidenceRepository ammoEvidenceRepository;

    private final Logger LOG = LogManager.getLogger();

    public AmmoInEvidenceService(AmmoInEvidenceRepository ammoInEvidenceRepository, AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository, ArmoryService armoryService, AmmoEvidenceRepository ammoEvidenceRepository) {
        this.ammoInEvidenceRepository = ammoInEvidenceRepository;
        this.ammoUsedToEvidenceEntityRepository = ammoUsedToEvidenceEntityRepository;
        this.armoryService = armoryService;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
    }

    boolean addAmmoUsedEntityToAmmoInEvidenceEntity(AmmoUsedToEvidenceEntity ammoUsedToEvidenceEntity) {
        //      Nie znaleziono żadnej listy
        if (!ammoEvidenceRepository.existsByOpenTrue()) {
            if (ammoUsedToEvidenceEntity.getCounter() < 0) {
                LOG.info("nie można dodać ujemnej wartości");
            } else {
//                nadawanie numeru listy
                int number;
                boolean all = ammoEvidenceRepository.countNumbers() < 1;
//                nadawanie numeru od zera
                if (all) {
                    number = 1;
//                nadawanie kolejnego numeru
                } else {
                    AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findAll()
                            .stream()
                            .filter(f -> f.getDate().getYear() == LocalDate.now().getYear())
                            .max(Comparator.comparing(a -> Integer.parseInt(a.getNumber().split("-")[0])))
                            .orElse(null);

                    if (ammoEvidenceEntity == null) {
                        number = 1;
                    } else {
                        String[] split = ammoEvidenceEntity.getNumber().split("-");
                        number = Integer.parseInt(split[0]) + 1;
                    }
                }
                String evidenceNumber = number + "-LA-" + LocalDate.now().getYear();
                AmmoEvidenceEntity buildEvidence = AmmoEvidenceEntity.builder()
                        .date(LocalDate.now())
                        .open(true)
                        .ammoInEvidenceEntityList(new ArrayList<>())
                        .number(evidenceNumber)
                        .build();
                ammoEvidenceRepository.save(buildEvidence);
                AmmoInEvidenceEntity build = AmmoInEvidenceEntity.builder()
                        .caliberName(ammoUsedToEvidenceEntity.getCaliberName())
                        .caliberUUID(ammoUsedToEvidenceEntity.getCaliberUUID())
                        .evidenceUUID(buildEvidence.getUuid())
                        .quantity(0)
                        .ammoUsedToEvidenceEntityList(new ArrayList<>())
                        .dateTime(LocalDateTime.now())
                        .build();

                build.getAmmoUsedToEvidenceEntityList().add(ammoUsedToEvidenceEntity);
                build.setQuantity(ammoUsedToEvidenceEntity.getCounter());
                ammoInEvidenceRepository.save(build);

                buildEvidence.getAmmoInEvidenceEntityList().add(build);
                ammoEvidenceRepository.save(buildEvidence);
                LOG.info("otworzono nową listę");
            }

        }
//      Znaleziono jakąś otwartą listę
        else {
            ammoEvidenceRepository.findAll();
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                    .findAll()
                    .stream()
                    .filter(AmmoEvidenceEntity::isOpen)
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);

            List<AmmoInEvidenceEntity> ammoInEvidenceEntityList = ammoEvidenceEntity.getAmmoInEvidenceEntityList();
//       Znaleziono jakąś listę z podanym kalibrem w ewidencji
            if (ammoInEvidenceEntityList
                    .stream()
                    .anyMatch(a -> a.getCaliberUUID().equals(ammoUsedToEvidenceEntity.getCaliberUUID()))) {


                AmmoInEvidenceEntity ammoInEvidenceEntity = ammoEvidenceEntity.getAmmoInEvidenceEntityList()
                        .stream()
                        .filter(f -> f.getCaliberUUID().equals(ammoUsedToEvidenceEntity.getCaliberUUID()))
                        .findFirst()
                        .orElseThrow(EntityNotFoundException::new);
                // Lista jest zablokowana - nie nastąpi dodanie amunicji do listy
                if (ammoInEvidenceEntity.isLocked()) {
                    LOG.info("Lista z kalibrem jest zablokowana - nie można dodać amunicji do listy");
                    return false;
                }

                List<AmmoUsedToEvidenceEntity> ammoUsedToEvidenceEntityList = ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList();
                if (ammoUsedToEvidenceEntityList.stream().noneMatch(f -> f.getName().equals(ammoUsedToEvidenceEntity.getName()))) {
                    if (ammoUsedToEvidenceEntity.getCounter() <= 0) {
                        LOG.info("nie można dodać ujemnej wartości");
                        return false;
                    } else {
                        ammoUsedToEvidenceEntityList.add(ammoUsedToEvidenceEntity);
                        ammoInEvidenceEntity.setQuantity(ammoInEvidenceEntity.getQuantity() + ammoUsedToEvidenceEntity.getCounter());

                        ammoInEvidenceRepository.save(ammoInEvidenceEntity);
                    }
                }
//        Znaleziono podanego membera
                else {
                    AmmoUsedToEvidenceEntity ammoUsedToEvidenceEntity1 = ammoUsedToEvidenceEntityList
                            .stream()
                            .filter(f -> f.getName()
                                    .equals(ammoUsedToEvidenceEntity.getName()))
                            .findFirst()
                            .orElseThrow(EntityNotFoundException::new);
                    if (ammoUsedToEvidenceEntity.getCounter() <= 0) {
//                      to jest to co należy dodać do magazynu amunicji     -ammoUsedToEvidenceEntity1.getCounter()
                        if (ammoUsedToEvidenceEntity1.getCounter() + ammoUsedToEvidenceEntity.getCounter() < 0) {
                            ammoUsedToEvidenceEntity.setCounter(-ammoUsedToEvidenceEntity1.getCounter());
                        }
                        armoryService.substratAmmo(ammoUsedToEvidenceEntity1.getCaliberUUID(), ammoUsedToEvidenceEntity.getCounter());
                    }
                    ammoUsedToEvidenceEntity1.setCounter(ammoUsedToEvidenceEntity1.getCounter() + ammoUsedToEvidenceEntity.getCounter());
                    ammoInEvidenceEntity.setQuantity(ammoInEvidenceEntity.getQuantity() + ammoUsedToEvidenceEntity.getCounter());


                    if (ammoUsedToEvidenceEntity1.getCounter() <= 0) {
                        ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().remove(ammoUsedToEvidenceEntity1);
                        ammoInEvidenceRepository.delete(ammoInEvidenceEntity);
                    } else {
                        ammoUsedToEvidenceEntityRepository.save(ammoUsedToEvidenceEntity1);
                        ammoInEvidenceRepository.save(ammoInEvidenceEntity);
                    }

                }

            }
//       Nie znaleziono żadnej listy z podanym kalibrem
            else {
                AmmoInEvidenceEntity build = AmmoInEvidenceEntity.builder()
                        .caliberName(ammoUsedToEvidenceEntity.getCaliberName())
                        .caliberUUID(ammoUsedToEvidenceEntity.getCaliberUUID())
                        .evidenceUUID(ammoEvidenceEntity.getUuid())
                        .quantity(0)
                        .dateTime(LocalDateTime.now())
                        .ammoUsedToEvidenceEntityList(new ArrayList<>())
                        .build();

                build.getAmmoUsedToEvidenceEntityList().add(ammoUsedToEvidenceEntity);
                build.setQuantity(ammoUsedToEvidenceEntity.getCounter());
                ammoInEvidenceRepository.save(build);
                ammoEvidenceEntity.getAmmoInEvidenceEntityList().add(build);
                ammoEvidenceRepository.save(ammoEvidenceEntity);
            }

        }
//          Usuwanie listy jeśli ilość sztuk wynosi 0
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findAllByOpenTrue()
                .stream()
                .findFirst()
                .orElse(null);
        if (ammoEvidenceEntity == null) {
            return false;
        }

        if (ammoEvidenceEntity
                .getAmmoInEvidenceEntityList()
                .stream()
                .anyMatch(a -> a.getQuantity() <= 0)) {

            AmmoInEvidenceEntity ammoInEvidenceEntity = ammoEvidenceEntity
                    .getAmmoInEvidenceEntityList()
                    .stream()
                    .filter(f -> f.getQuantity() <= 0)
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);

            ammoEvidenceEntity.getAmmoInEvidenceEntityList().remove(ammoInEvidenceEntity);
            ammoInEvidenceRepository.delete(ammoInEvidenceEntity);
            //        Usuwanie ewidencji jeśli nie ma żadnej listy z amunicją
            if (ammoEvidenceEntity.getAmmoInEvidenceEntityList().isEmpty()) {
                ammoEvidenceRepository.delete(ammoEvidenceEntity);
            }
        }
        return true;

    }

}
