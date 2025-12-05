package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmmoEvidenceService {


    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final HistoryService historyService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public AmmoEvidenceService(AmmoEvidenceRepository ammoEvidenceRepository, AmmoInEvidenceRepository ammoInEvidenceRepository, HistoryService historyService) {
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.ammoInEvidenceRepository = ammoInEvidenceRepository;
        this.historyService = historyService;
    }

    public ResponseEntity<?> getOpenEvidence() {
        List<AmmoEvidenceEntity> allByOpenTrue = ammoEvidenceRepository.findAllByOpenTrue();
        if (allByOpenTrue.size() > 0) {
            return ResponseEntity.ok(Mapping.map(allByOpenTrue.get(0)));
        }
        return ResponseEntity.ok(null);
//        return ammoEvidenceRepository.findAllByOpenTrue().size() > 0 ? ResponseEntity.ok(Mapping.map(ammoEvidenceRepository.findAllByOpenTrue().get(0))) : ResponseEntity.ok(new ArrayList<>());
    }

    public AmmoEvidenceDTO getEvidence(String uuid) {
        return Mapping.map(ammoEvidenceRepository.getOne(uuid));
    }

    public void automationCloseEvidence() {
        List<AmmoEvidenceEntity> allByOpenTrue = ammoEvidenceRepository.findAllByOpenTrue();
        allByOpenTrue.forEach(e -> {
            closeEvidence(e.getUuid());
            LOG.info("Lista zamknięta automatycznie");
        });
    }

    public ResponseEntity<?> closeEvidence(String evidenceUUID) {
        if (!ammoEvidenceRepository.existsById(evidenceUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono listy");
        }
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                .findById(evidenceUUID)
                .orElseThrow(EntityNotFoundException::new);
        ammoEvidenceEntity.setOpen(false);
        ammoEvidenceEntity.setForceOpen(false);
        ammoEvidenceRepository.save(ammoEvidenceEntity);
        LOG.info("zamknięto listę " + ammoEvidenceEntity.getNumber() + " z dnia " + ammoEvidenceEntity.getDate());
        return ResponseEntity.ok("Lista została zamknięta");
    }

    public List<AmmoDTO> getClosedEvidences(Pageable page) {
        page = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by("date").and(Sort.by("number")).descending());
        return ammoEvidenceRepository.findAllByOpenFalse(page).map(Mapping::map1).toList();
    }

    public ResponseEntity<?> openEvidence(String evidenceUUID, String pinCode) throws NoUserPermissionException {
        if (ammoEvidenceRepository.findAll().stream().anyMatch(AmmoEvidenceEntity::isOpen)) {
            return ResponseEntity.badRequest().body("Nie można otworzyć listy bo inna jest otwarta");
        }

        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.getOne(evidenceUUID);
        if (ammoEvidenceEntity.isLocked()) {
            return ResponseEntity.badRequest().body("Lista Została zablokowana - nie można jej już otworzyć");
        }
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, ammoEvidenceEntity, HttpStatus.OK, "openEvidence", "Ręcznie otworzono listę - Pamiętaj by ją zamknąć!");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            ammoEvidenceEntity.setOpen(true);
            ammoEvidenceEntity.setForceOpen(true);
            ammoEvidenceRepository.save(ammoEvidenceEntity);
            LOG.info("otworzono");
        }
        return response;

    }

    public String checkAnyOpenEvidence() {
        // send name of colours with -> \" \" <-
        // primary
        // secondary
        // accent
        // warning
        // negative
        // rest of colours
        String message = "\"primary\"";

        if (ammoEvidenceRepository.findAll().stream().anyMatch(f -> f.isOpen() && !f.isForceOpen())) {
            message = "\"negative\"";
        }
        if (ammoEvidenceRepository.findAll().stream().anyMatch(f -> f.isOpen() && f.isForceOpen())) {
            message = "\"red\"";
        }
        return message;
    }

    public List<AmmoEvidenceDTO> getNotLockedEvidences() {
        return ammoEvidenceRepository.findAll().stream().filter(f -> !f.isLocked()).map(Mapping::map).sorted(Comparator.comparing(AmmoEvidenceDTO::getDate).reversed()).collect(Collectors.toList());
    }

    public List<?> getAmmoInEvidece(String caliberUUID) {
        return ammoInEvidenceRepository.findAll().stream().filter(f -> f.getCaliberUUID().equals(caliberUUID)).filter(f -> f.isLocked() && f.getSignedBy() != null).map(Mapping::map).sorted(Comparator.comparing(AmmoInEvidenceDTO::getDate).reversed()).collect(Collectors.toList());
    }
}
