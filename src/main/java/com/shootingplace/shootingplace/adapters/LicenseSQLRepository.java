package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.license.LicenseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface LicenseSQLRepository extends LicenseRepository, JpaRepository<LicenseEntity,String> {
}
