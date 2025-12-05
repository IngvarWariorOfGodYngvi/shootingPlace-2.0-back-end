package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShootingPacketService {

    private final ShootingPacketRepository shootingPacketRepository;
    private final CaliberForShootingPacketRepository caliberForShootingPacketRepository;
    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;
    private final HistoryService historyService;
    private final CaliberRepository caliberRepository;

    public ShootingPacketService(ShootingPacketRepository shootingPacketRepository, CaliberForShootingPacketRepository caliberForShootingPacketRepository, WorkingTimeEvidenceRepository workingTimeEvidenceRepository, HistoryService historyService, CaliberRepository caliberRepository) {
        this.shootingPacketRepository = shootingPacketRepository;
        this.caliberForShootingPacketRepository = caliberForShootingPacketRepository;
        this.workingTimeEvidenceRepository = workingTimeEvidenceRepository;
        this.historyService = historyService;
        this.caliberRepository = caliberRepository;
    }

    public List<ShootingPacketDTO> getAllShootingPacket() {
        return shootingPacketRepository.findAll().stream().map(Mapping::map).sorted(Comparator.comparing(ShootingPacketDTO::getPrice)).collect(Collectors.toList());
    }

    public List<ShootingPacketEntity> getAllShootingPacketEntities() {
        return shootingPacketRepository.findAll().stream().sorted(Comparator.comparing(ShootingPacketEntity::getPrice)).collect(Collectors.toList());
    }

    public ResponseEntity<?> addShootingPacket(String name, float price, Map<String, Integer> map, String pinCode) throws NoUserPermissionException {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }
        List<CaliberForShootingPacketEntity> entities = new ArrayList<>();
        map.forEach((k, v) -> entities.add(caliberForShootingPacketRepository.save(CaliberForShootingPacketEntity.builder().caliberUUID(k).caliberName(caliberRepository.getOne(k).getName()).quantity(v).build())));
        return historyService.getStringResponseEntity(pinCode, shootingPacketRepository.save(ShootingPacketEntity.builder().name(name.toUpperCase()).price(price).calibers(entities).build()), HttpStatus.OK, "addShootingPacket", "utworzono pakiet strzelecki " + name.toUpperCase());
    }


    public List<CaliberForShootingPacketEntity> getAllCalibersFromShootingPacket(String shootingPacketUUID) {
        return shootingPacketRepository.getOne(shootingPacketUUID).getCalibers();
    }

    public ResponseEntity<?> updateShootingPacket(String uuid, String name, Float price, Map<String, Integer> map, String pinCode) throws NoUserPermissionException {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }
        List<CaliberForShootingPacketEntity> entities = new ArrayList<>();
        map.forEach((k, v) -> entities.add(caliberForShootingPacketRepository.save(CaliberForShootingPacketEntity.builder()
                .caliberUUID(k)
                .caliberName(caliberRepository.getOne(k).getName())
                .quantity(v)
                .build())));

        ShootingPacketEntity one = shootingPacketRepository.getOne(uuid);

        if (name != null && !name.isEmpty() && !name.equals(one.getName())) {
            one.setName(name);
        }
        if (price != null && price != one.getPrice()) {
            one.setPrice(price);
        }
        if (!entities.isEmpty()) {
            boolean b = true;
            for (CaliberForShootingPacketEntity entity : entities) {
                if (entity.getQuantity() <= 0) {
                    b = false;
                    break;
                }
            }

            if (b) one.setCalibers(entities);
        }


        return historyService.getStringResponseEntity(pinCode, shootingPacketRepository.save(one), HttpStatus.OK, "updateShootingPacket", "edytowano pakiet strzelecki " + one.getName().toUpperCase());
    }

    public ResponseEntity<?> deleteShootingPacket(String uuid, String pinCode) throws NoUserPermissionException {
        ShootingPacketEntity one = shootingPacketRepository.getOne(uuid);
        shootingPacketRepository.delete(shootingPacketRepository.getOne(uuid));
        return historyService.getStringResponseEntity(pinCode, one,HttpStatus.OK, "deleteShootingPacket","usunięto pakiet Strzelecki");
    }
}
