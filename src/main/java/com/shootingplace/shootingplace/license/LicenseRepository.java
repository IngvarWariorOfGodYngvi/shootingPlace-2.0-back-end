package com.shootingplace.shootingplace.license;

public interface LicenseRepository {

    LicenseEntity save(LicenseEntity entity);

    LicenseEntity getOne(String uuid);

    boolean existsById(String uuid);
}
