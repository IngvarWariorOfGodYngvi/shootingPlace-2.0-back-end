package com.shootingplace.shootingplace.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LicensePaymentHistoryRepository extends JpaRepository<LicensePaymentHistoryEntity, String> {
    @Query(nativeQuery = true,value = "select * from shootingplace.license_payment_history_entity where is_pay_inpzssportal = false")
    List<LicensePaymentHistoryEntity> findAllByPayInPZSSPortalFalse();
    @Query(nativeQuery = true,value = "select * from shootingplace.license_payment_history_entity where is_pay_inpzssportal = true")
    List<LicensePaymentHistoryEntity> findAllByPayInPZSSPortalTrue();

    @Query(nativeQuery = true,value = "select * from shootingplace.license_payment_history_entity where (date between (:firstDate) and (:secondDate))")
    List<LicensePaymentHistoryEntity> findAllByPayInPZSSPortalBetweenDate(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);
}
