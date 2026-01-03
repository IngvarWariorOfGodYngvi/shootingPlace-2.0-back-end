package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmmoEvidenceService {


    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final Logger LOG = LogManager.getLogger(getClass());

    public ResponseEntity<?> getOpenEvidence() {
        List<AmmoEvidenceEntity> allByOpenTrue = ammoEvidenceRepository.findAllByOpenTrue();
        if (!allByOpenTrue.isEmpty()) {
            return ResponseEntity.ok(Mapping.map(allByOpenTrue.getFirst()));
        }
        return ResponseEntity.ok(null);
//        return ammoEvidenceRepository.findAllByOpenTrue().size() > 0 ? ResponseEntity.ok(Mapping.map(ammoEvidenceRepository.findAllByOpenTrue().get(0))) : ResponseEntity.ok(new ArrayList<>());
    }

    public AmmoEvidenceDTO getEvidence(String uuid) {
        return Mapping.map(ammoEvidenceRepository.findById(uuid).orElseThrow(EntityNotFoundException::new));
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
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findById(evidenceUUID).orElseThrow(EntityNotFoundException::new);
        ammoEvidenceEntity.setOpen(false);
        ammoEvidenceEntity.setForceOpen(false);
        ammoEvidenceRepository.save(ammoEvidenceEntity);
        LOG.info("zamknięto listę {} z dnia {}", ammoEvidenceEntity.getNumber(), ammoEvidenceEntity.getDate());
        return ResponseEntity.ok("Lista została zamknięta");
    }

    public List<AmmoDTO> getClosedEvidences(Pageable page) {
        page = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by("date").and(Sort.by("number")).descending());
        return ammoEvidenceRepository.findAllByOpenFalse(page).map(Mapping::map1).toList();
    }

    @Transactional
    @RecordHistory(action = "AmmoEvidence.open", entity = HistoryEntityType.AMMO_EVIDENCE, entityArgIndex = 0)
    public ResponseEntity<?> openEvidence(String evidenceUUID) {

        boolean anyOpen = ammoEvidenceRepository.findAll().stream().anyMatch(AmmoEvidenceEntity::isOpen);
        if (anyOpen) {
            return ResponseEntity.badRequest().body("Nie można otworzyć listy – inna lista jest już otwarta");
        }
        AmmoEvidenceEntity evidence = ammoEvidenceRepository.findById(evidenceUUID).orElse(null);
        if (evidence == null) {
            return ResponseEntity.badRequest().body("Nie znaleziono listy ewidencji");
        }
        if (evidence.isLocked()) {
            return ResponseEntity.badRequest().body("Lista została zablokowana – nie można jej otworzyć");
        }
        evidence.setOpen(true);
        evidence.setForceOpen(true);
        ammoEvidenceRepository.save(evidence);
        LOG.info("Ręcznie otworzono listę ewidencji amunicji {}", evidenceUUID);
        return ResponseEntity.ok("Ręcznie otworzono listę – pamiętaj, aby ją zamknąć");
    }


    public boolean checkAnyOpenEvidence() {
        return ammoEvidenceRepository.findAll().stream().anyMatch(f -> f.isOpen() && f.isForceOpen());
    }

    public List<AmmoEvidenceDTO> getNotLockedEvidences() {
        return ammoEvidenceRepository.findAll().stream().filter(f -> !f.isLocked()).map(Mapping::map).sorted(Comparator.comparing(AmmoEvidenceDTO::getDate).reversed()).collect(Collectors.toList());
    }

    public List<?> getAmmoInEvidence(String caliberUUID) {
        return ammoInEvidenceRepository.findAll().stream().filter(f -> f.getCaliberUUID().equals(caliberUUID)).filter(f -> f.isLocked() && f.getSignedBy() != null).map(Mapping::map).sorted(Comparator.comparing(AmmoInEvidenceDTO::getDate).reversed()).collect(Collectors.toList());
    }
}
