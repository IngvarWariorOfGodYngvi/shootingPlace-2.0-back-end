package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceRepository;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.security.UserAuthContext;
import com.shootingplace.shootingplace.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaliberService {

    private final CaliberRepository caliberRepository;
    private final CalibersAddedRepository calibersAddedRepository;
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final UserAuthContext userAuthContext;
    private final Logger LOG = LogManager.getLogger(getClass());

    public List<CaliberEntity> getCalibersEntityList() {
        List<CaliberEntity> caliberEntityList;
        caliberEntityList = caliberRepository.findAll();
        return getCaliberSortedEntityList(caliberEntityList);

    }

    public List<Caliber> getCalibersList() {
        List<Caliber> caliberList;
        caliberList = caliberRepository.findAll().stream().filter(CaliberEntity::isActive).map(this::map).collect(Collectors.toList());

        return caliberList;

    }

    public List<Caliber> calibersForEvidence() {
        List<Caliber> caliberList;
        caliberList = caliberRepository.findAll().stream().map(this::map).collect(Collectors.toList());

        return caliberList;

    }

    public int getCalibersQuantity(String uuid, LocalDate date) {
        List<CalibersAddedEntity> collect = calibersAddedRepository.findAll().stream().filter(f -> f.getBelongTo().equals(uuid)).filter(f -> f.getDate().isBefore(date.plusDays(1))).toList();
        AtomicReference<Integer> count = new AtomicReference<>(0);
        if (!collect.isEmpty()) {
            collect.forEach(f -> count.updateAndGet(v -> v + f.getAmmoAdded()));
        }
        List<AmmoInEvidenceEntity> collect1 = new ArrayList<>();
        ammoEvidenceRepository.findAll().stream().filter(f -> f.getDate().isBefore(date.plusDays(1))).forEach(e -> {
            List<AmmoInEvidenceEntity> collect2 = e.getAmmoInEvidenceEntityList().stream().filter(f -> f.getCaliberUUID().equals(uuid)).toList();
            collect1.addAll(collect2);
        });
        AtomicReference<Integer> count1 = new AtomicReference<>(0);
        if (!collect1.isEmpty()) {
            collect1.forEach(g -> count1.updateAndGet(v -> v + g.getQuantity()));
        }
        Integer opaque = count.getOpaque();
        Integer opaque1 = count1.getOpaque();
        return opaque - opaque1;
    }

    private Caliber map(CaliberEntity c) {
        return Optional.ofNullable(c).map(e -> Caliber.builder().name(e.getName()).uuid(e.getUuid()).active(e.isActive()).quantity(getCaliberAmmoInStore(c.getUuid())).unitPrice(e.getUnitPrice()).unitPriceForNotMember(e.getUnitPriceForNotMember()).build()).orElse(null);
    }

    public int getCaliberAmmoInStore(String uuid) {
        List<CalibersAddedEntity> collect = calibersAddedRepository.findAll().stream().filter(f -> f.getBelongTo().equals(uuid)).toList();
        AtomicReference<Integer> count = new AtomicReference<>(0);
        collect.forEach(f -> count.updateAndGet(v -> v + f.getAmmoAdded()));
        List<AmmoInEvidenceEntity> collect1 = ammoInEvidenceRepository.findAll().stream().filter(f -> f.getCaliberUUID().equals(uuid)).toList();
        AtomicReference<Integer> count1 = new AtomicReference<>(0);
        collect1.forEach(g -> count1.updateAndGet(v -> v + g.getQuantity()));
        return count.getOpaque() - count1.getOpaque();
    }

    private List<CaliberEntity> getCaliberSortedEntityList(List<CaliberEntity> caliberEntityList) {
        String[] sort = {"5,6mm", "9x19mm", "12/76", ".357", ".38", "7,62x39mm"};
        List<CaliberEntity> collect = caliberEntityList.stream().filter(f -> !f.getName().equals(sort[0]) && !f.getName().equals(sort[1]) && !f.getName().equals(sort[2]) && !f.getName().equals(sort[3]) && !f.getName().equals(sort[4]) && !f.getName().equals(sort[5])).toList();

        CaliberEntity caliberEntity = caliberEntityList.stream().filter(f -> f.getName().equals(sort[0])).findFirst().orElse(null);
        CaliberEntity caliberEntity1 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[1])).findFirst().orElse(null);
        CaliberEntity caliberEntity2 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[2])).findFirst().orElse(null);
        CaliberEntity caliberEntity3 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[3])).findFirst().orElse(null);
        CaliberEntity caliberEntity4 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[4])).findFirst().orElse(null);
        CaliberEntity caliberEntity5 = caliberEntityList.stream().filter(f -> f.getName().equals(sort[5])).findFirst().orElse(null);
        List<CaliberEntity> caliberEntityList2 = new ArrayList<>();
        caliberEntityList2.add(caliberEntity);
        caliberEntityList2.add(caliberEntity1);
        caliberEntityList2.add(caliberEntity2);
        caliberEntityList2.add(caliberEntity3);
        caliberEntityList2.add(caliberEntity4);
        caliberEntityList2.add(caliberEntity5);
        caliberEntityList2.addAll(collect);
        return caliberEntityList2;
    }

    @Transactional
    @RecordHistory(action = "Caliber.create", entity = HistoryEntityType.CALIBER)
    public ResponseEntity<?> createNewCaliber(String caliber) {
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        String normalized = caliber.trim().toLowerCase();
        boolean exists = caliberRepository.findAll().stream().anyMatch(c -> c.getName().equals(normalized));
        if (exists) {
            return ResponseEntity.badRequest().body("Kaliber już istnieje");
        }
        CaliberEntity caliberEntity = CaliberEntity.builder().name(normalized).quantity(0).active(true).ammoAdded(null).ammoUsed(null).build();
        caliberRepository.save(caliberEntity);
        LOG.info("Utworzono nowy kaliber {}", normalized);
        return ResponseEntity.ok("Utworzono nowy kaliber");
    }


    @Transactional
    @RecordHistory(action = "Caliber.toggleActive", entity = HistoryEntityType.CALIBER, entityArgIndex = 0)
    public ResponseEntity<?> activateOrDeactivateCaliber(String caliberUUID) {
        CaliberEntity caliber = caliberRepository.findById(caliberUUID).orElse(null);
        if (caliber == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono kalibru");
        }
        caliber.changeActive();
        caliberRepository.save(caliber);
        LOG.info("{} kaliber {}", caliber.isActive() ? "Aktywowano" : "Dezaktywowano", caliber.getName());
        return ResponseEntity.ok(caliber.isActive() ? "Aktywowano Kaliber" : "Dezaktywowano Kaliber");
    }

}
