package com.shootingplace.shootingplace.history;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LicensePaymentHistoryRepository {
    @Query(nativeQuery = true,value = "select * from shootingplace.license_payment_history_entity where is_pay_inpzssportal = false")
    List<LicensePaymentHistoryEntity> findAllByPayInPZSSPortalFalse();
    @Query(nativeQuery = true,value = "select * from shootingplace.license_payment_history_entity where is_pay_inpzssportal = true")
    List<LicensePaymentHistoryEntity> findAllByPayInPZSSPortalTrue();

    @Query(nativeQuery = true,value = "select * from shootingplace.license_payment_history_entity where (date between (:firstDate) and (:secondDate))")
    List<LicensePaymentHistoryEntity> findAllByPayInPZSSPortalBetweenDate(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

    LicensePaymentHistoryEntity save(LicensePaymentHistoryEntity entity);

    boolean existsById(String uuid);

    Optional<LicensePaymentHistoryEntity> findById(String uuid);

    void delete(LicensePaymentHistoryEntity entity);

    List<LicensePaymentHistoryEntity> findAll();
}
