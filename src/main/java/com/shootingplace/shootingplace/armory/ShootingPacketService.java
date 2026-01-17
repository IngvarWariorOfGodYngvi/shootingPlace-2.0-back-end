package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.utils.Mapping;
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
    private final CaliberRepository caliberRepository;

    private final Logger LOG = LogManager.getLogger();

    public List<ShootingPacketDTO> getAllShootingPacket() {
        return shootingPacketRepository.findAll().stream().map(Mapping::map).sorted(Comparator.comparing(ShootingPacketDTO::getPrice)).collect(Collectors.toList());
    }

    public List<ShootingPacketEntity> getAllShootingPacketEntities() {
        return shootingPacketRepository.findAll().stream().sorted(Comparator.comparing(ShootingPacketEntity::getPrice)).collect(Collectors.toList());
    }

    public List<CaliberForShootingPacketEntity> getAllCalibersFromShootingPacket(String shootingPacketUUID) {
        return shootingPacketRepository.findById(shootingPacketUUID).orElseThrow(EntityNotFoundException::new).getCalibers();
    }

    @Transactional
    @RecordHistory(action = "ShootingPacket.create", entity = HistoryEntityType.SHOOTING_PACKET)
    public ResponseEntity<?> addShootingPacket(String name, float price, Map<String, Integer> calibers) {
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
    public ResponseEntity<?> updateShootingPacket(String uuid, ShootingPacketDTO newPacket) {
        ShootingPacketEntity packet = shootingPacketRepository.findById(uuid).orElseThrow(() -> new DomainNotFoundException("ShootingPacket", uuid));

        if (newPacket.getName() != null && !newPacket.getName().isBlank() && !newPacket.getName().equalsIgnoreCase(packet.getName())) {
            packet.setName(newPacket.getName().toUpperCase());
        }

        if (newPacket.getPrice() != null && !newPacket.getPrice().equals(packet.getPrice())) {
            packet.setPrice(newPacket.getPrice());
        }

        if (newPacket.getCalibers() != null && !newPacket.getCalibers().isEmpty()) {
            List<CaliberForShootingPacketEntity> caliberEntities = new ArrayList<>();
            newPacket.getCalibers().forEach(e -> {
                CaliberEntity caliber = caliberRepository.findById(e.getCaliberUUID()).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kalibru"));
                caliberEntities.add(caliberForShootingPacketRepository.save(CaliberForShootingPacketEntity.builder()
                        .caliberUUID(e.getCaliberUUID())
                        .caliberName(caliber.getName())
                        .quantity(e.getQuantity())
                        .build()));
            });
            packet.setCalibers(caliberEntities);
        }
        shootingPacketRepository.save(packet);

        LOG.info("Zaktualizowano pakiet strzelecki {}", packet.getName());

        return ResponseEntity.ok("Zedytowano pakiet strzelecki " + packet.getName());
    }


    @Transactional
    @RecordHistory(action = "ShootingPacket.delete", entity = HistoryEntityType.SHOOTING_PACKET, entityArgIndex = 0)
    public ResponseEntity<?> deleteShootingPacket(String uuid) {
        ShootingPacketEntity packet = shootingPacketRepository.findById(uuid).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono pakietu"));

        shootingPacketRepository.delete(packet);

        LOG.info("Usunięto pakiet strzelecki {}", packet.getName());

        return ResponseEntity.ok("Usunięto pakiet strzelecki");
    }

}
