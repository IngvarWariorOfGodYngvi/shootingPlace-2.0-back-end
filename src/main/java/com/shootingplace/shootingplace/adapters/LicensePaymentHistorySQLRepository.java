package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.history.LicensePaymentHistoryEntity;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicensePaymentHistorySQLRepository extends LicensePaymentHistoryRepository, JpaRepository<LicensePaymentHistoryEntity,String> {
}
