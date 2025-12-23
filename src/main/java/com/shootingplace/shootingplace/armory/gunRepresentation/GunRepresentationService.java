package com.shootingplace.shootingplace.armory.gunRepresentation;

import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.armory.GunUsedEntity;
import com.shootingplace.shootingplace.armory.GunUsedRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GunRepresentationService {

    private final GunRepresentationRepository gunRepresentationRepository;
    private final GunRepository gunRepository;
    private final GunUsedRepository gunUsedRepository;
    private final Logger LOG = LogManager.getLogger(getClass());

    public void function(GunUsedEntity gunUsedEntity) {
        if (gunUsedEntity.getGunRepresentationEntity() == null) {
            GunEntity one = gunRepository.findById(gunUsedEntity.getGunUUID()).orElseThrow(EntityNotFoundException::new);

            GunRepresentationEntity representation = Mapping.mapToRepresentation(one);

            GunRepresentationEntity gunRepresentation = gunRepresentationRepository.save(representation);

            gunUsedEntity.setGunRepresentationEntity(gunRepresentation);

            gunUsedRepository.save(gunUsedEntity);
            LOG.info("Zmieniam");
        }
    }
}
