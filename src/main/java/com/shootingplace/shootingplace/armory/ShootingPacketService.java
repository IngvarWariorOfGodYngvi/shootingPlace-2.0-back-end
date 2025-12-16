package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.history.RecordHistory;
import com.shootingplace.shootingplace.utils.Mapping;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShootingPacketService {

    private final ShootingPacketRepository shootingPacketRepository;
    private final CaliberForShootingPacketRepository caliberForShootingPacketRepository;
    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;
    private final HistoryService historyService;
    private final CaliberRepository caliberRepository;

    private final Logger LOG = LogManager.getLogger();

    public List<ShootingPacketDTO> getAllShootingPacket() {
        return shootingPacketRepository.findAll().stream().map(Mapping::map).sorted(Comparator.comparing(ShootingPacketDTO::getPrice)).collect(Collectors.toList());
    }

    public List<ShootingPacketEntity> getAllShootingPacketEntities() {
        return shootingPacketRepository.findAll().stream().sorted(Comparator.comparing(ShootingPacketEntity::getPrice)).collect(Collectors.toList());
    }

    public List<CaliberForShootingPacketEntity> getAllCalibersFromShootingPacket(String shootingPacketUUID) {
        return shootingPacketRepository.getOne(shootingPacketUUID).getCalibers();
    }

    @Transactional
    @RecordHistory(action = "ShootingPacket.create", entity = HistoryEntityType.SHOOTING_PACKET)
    public ResponseEntity<?> addShootingPacket(String name, float price, Map<String, Integer> calibers, String pinCode) {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }

        List<CaliberForShootingPacketEntity> caliberEntities = new ArrayList<>();

        calibers.forEach((caliberUUID, quantity) -> {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Ilość amunicji musi być większa od 0");
            }

            CaliberEntity caliber = caliberRepository.findById(caliberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kalibru"));

            caliberEntities.add(caliberForShootingPacketRepository.save(CaliberForShootingPacketEntity.builder().caliberUUID(caliberUUID).caliberName(caliber.getName()).quantity(quantity).build()));
        });

        ShootingPacketEntity packet = shootingPacketRepository.save(ShootingPacketEntity.builder().name(name.toUpperCase()).price(price).calibers(caliberEntities).build());

        LOG.info("Utworzono pakiet strzelecki {}", packet.getName());

        return ResponseEntity.ok("Utworzono pakiet strzelecki " + packet.getName());
    }


    @Transactional
    @RecordHistory(action = "ShootingPacket.update", entity = HistoryEntityType.SHOOTING_PACKET, entityArgIndex = 0)
    public ResponseEntity<?> updateShootingPacket(String uuid, String name, Float price, Map<String, Integer> calibers, String pinCode) {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }
        ShootingPacketEntity packet = shootingPacketRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono pakietu"));

        if (name != null && !name.isBlank() && !name.equalsIgnoreCase(packet.getName())) {
            packet.setName(name.toUpperCase());
        }

        if (price != null && !price.equals(packet.getPrice())) {
            packet.setPrice(price);
        }

        if (calibers != null && !calibers.isEmpty()) {
            List<CaliberForShootingPacketEntity> caliberEntities = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : calibers.entrySet()) {
                if (entry.getValue() <= 0) {
                    return ResponseEntity.badRequest().body("Ilość amunicji musi być większa od 0");
                }
                CaliberEntity caliber = caliberRepository.findById(entry.getKey()).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kalibru"));
                caliberEntities.add(caliberForShootingPacketRepository.save(CaliberForShootingPacketEntity.builder().caliberUUID(entry.getKey()).caliberName(caliber.getName()).quantity(entry.getValue()).build()));
            }
            packet.setCalibers(caliberEntities);
        }
        shootingPacketRepository.save(packet);

        LOG.info("Zaktualizowano pakiet strzelecki {}", packet.getName());

        return ResponseEntity.ok("Zedytowano pakiet strzelecki " + packet.getName());
    }


    @Transactional
    @RecordHistory(action = "ShootingPacket.delete", entity = HistoryEntityType.SHOOTING_PACKET, entityArgIndex = 0)
    public ResponseEntity<?> deleteShootingPacket(String uuid, String pinCode) {
        ShootingPacketEntity packet = shootingPacketRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono pakietu"));

        shootingPacketRepository.delete(packet);

        LOG.info("Usunięto pakiet strzelecki {}", packet.getName());

        return ResponseEntity.ok("Usunięto pakiet strzelecki");
    }

}
