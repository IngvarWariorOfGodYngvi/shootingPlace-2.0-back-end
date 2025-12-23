package com.shootingplace.shootingplace.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {

    Optional<MemberEntity> findByPesel(String pesel);

    Optional<MemberEntity> findByEmail(String email);

    Optional<MemberEntity> findByLegitimationNumber(Integer legitimationNumber);

    Optional<MemberEntity> findByClubCardBarCode(String barCode);

    Optional<MemberEntity> findByPhoneNumber(String phoneNumber);

    Optional<MemberEntity> findByIDCard(String IDCard);

    boolean existsByLegitimationNumber(Integer legitimationNumber);

    List<MemberEntity> findAllByJoinDate(LocalDate localDate);

    List<MemberEntity> findAllByErasedFalse();

    List<MemberEntity> findAllByErasedFalseAndActiveFalse();

    List<MemberEntity> findAllByErasedFalseAndActiveTrue();

    Page<MemberEntity> findAllByErasedFalse(Pageable pageable);

    List<MemberEntity> findAllByErasedTrue();

    List<MemberEntity> findAllByErasedFalseAndMemberPermissions_ArbiterNumberIsNotNull();

    MemberEntity findByHistoryUuid(String historyUUID);

    MemberEntity findByLicenseUuid(String licenseUUID);

    @Query(value = """
                SELECT m.*
                FROM shootingplace.member_entity m
                JOIN shootingplace.license_entity l 
                  ON m.license_uuid = l.uuid
                WHERE m.club_id = 1
                  AND m.erased = false
                  AND m.pzss = true
                  AND l.number > 0
                  AND l.valid = true
            """, nativeQuery = true)
    List<MemberEntity> findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue();

    @Query(value = """
                SELECT m.*
                FROM shootingplace.member_entity m
                JOIN shootingplace.license_entity l 
                  ON m.license_uuid = l.uuid
                WHERE m.club_id = 1
                  AND m.erased = false
                  AND m.pzss = true
                  AND l.number > 0
                  AND l.valid = false
            """, nativeQuery = true)
    List<MemberEntity> findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidFalse();

    @Query(value = """
                SELECT COUNT(*)
                FROM shootingplace.member_entity
                WHERE join_date BETWEEN :start AND :stop
            """, nativeQuery = true)
    int countActualYearMemberCounts(@Param("start") LocalDate start, @Param("stop") LocalDate stop);

    @Query(value = """
                SELECT 
                  uuid, first_name, second_name, imageuuid,
                  active, adult, erased, pzss,
                  legitimation_number, join_date, note, email
                FROM shootingplace.member_entity
                WHERE join_date BETWEEN :firstDate AND :secondDate
                ORDER BY join_date
            """, nativeQuery = true)
    List<IMemberDTO> getMemberBetweenJoinDate(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

    @Query(value = """
                SELECT 
                  uuid, first_name, second_name, imageuuid,
                  active, adult, erased, pzss,
                  legitimation_number, join_date, note, email
                FROM shootingplace.member_entity
                WHERE history_uuid = :historyUUID
            """, nativeQuery = true)
    IMemberDTO getByHistoryUUID(@Param("historyUUID") String historyUUID);

    @Query(value = "SELECT MAX(legitimation_number) FROM shootingplace.member_entity", nativeQuery = true)
    int getMaxLegitimationNumber();

    @Query(value = """
                SELECT m.*
                FROM shootingplace.member_entity m
                WHERE m.adult = false
                  AND m.erased = false
            """, nativeQuery = true)
    List<MemberEntity> findAllByAdultFalseAndErasedFalse();

    @Query(value = "SELECT m.* FROM shootingplace.member_entity m WHERE m.club_id = 1 AND m.erased = false AND m.pzss = true", nativeQuery = true)
    List<MemberEntity> findAllWhereClubEquals1ErasedFalsePzssTrue();
}
