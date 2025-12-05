package com.shootingplace.shootingplace.ammoEvidence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AmmoEvidenceRepository {
    List<AmmoEvidenceEntity> findAll();

    List<AmmoEvidenceEntity> findAllByOpenTrueAndForceOpenFalse();

    boolean existsByOpenTrueAndForceOpenFalse();

    boolean existsById(String uuid);

    AmmoEvidenceEntity getOne(String uuid);

    Optional<AmmoEvidenceEntity> findById(String uuid);

    AmmoEvidenceEntity save(AmmoEvidenceEntity entity);

    void delete(AmmoEvidenceEntity entity);

    Page<AmmoEvidenceEntity> findAll(Pageable page);

    Page<AmmoEvidenceEntity> findAllByOpenFalse(Pageable page);

    List<AmmoEvidenceEntity> findAllByOpenTrue();

    boolean existsByOpenTrue();

    @Query(nativeQuery = true, value = "Select count('number') FROM shootingplace.ammo_evidence_entity")
    long countNumbers();

    @Query(nativeQuery = true, value = "Select * from shootingplace.ammo_evidence_entity where (date between (:firstDate) and (:secondDate))")
    List<AmmoEvidenceEntity> getAllDateBetween(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

    AmmoEvidenceEntity getByOpenTrue();

    List<AmmoEvidenceEntity> findAllByLockedFalse();
}
