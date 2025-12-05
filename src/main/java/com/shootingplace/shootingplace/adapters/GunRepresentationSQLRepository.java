package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationEntity;
import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GunRepresentationSQLRepository extends GunRepresentationRepository, JpaRepository<GunRepresentationEntity,String> {
}
