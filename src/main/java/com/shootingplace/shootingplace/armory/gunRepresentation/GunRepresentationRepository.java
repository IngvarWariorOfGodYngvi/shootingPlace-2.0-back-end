package com.shootingplace.shootingplace.armory.gunRepresentation;

public interface GunRepresentationRepository{

    GunRepresentationEntity getOne(String uuid);

    GunRepresentationEntity save(GunRepresentationEntity representation);
}
