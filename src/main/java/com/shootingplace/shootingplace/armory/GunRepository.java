package com.shootingplace.shootingplace.armory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GunRepository extends JpaRepository<GunEntity, String> {

    Optional<GunEntity> findByImgUUID(String uuid);

    Optional<GunEntity> findByBarcode(String barcode);

    List<GunEntity> findAllByInUseStatus(String status);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByGunCertificateSerialNumber(String gunCertificateSerialNumber);

    boolean existsByRecordInEvidenceBook(String recordInEvidenceBook);

    boolean existsByBarcode(String barcode);
}
