package com.shootingplace.shootingplace.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SentEmailRepository extends JpaRepository<SentEmail, String> {
    @Query(nativeQuery = true, value = "SELECT * from shootingplace.sent_email WHERE memberuuid = (:memberUUID) AND mail_type = (:mailType) AND success AND sent_at BETWEEN (:firstDate) AND (:secondDate)")
    List<SentEmail> getEmailsByMemberUUIDAndMailTypeAndSuccessTrueAndSentAtIsBetween(@Param("memberUUID") String memberUUID, @Param("mailType") String mailType, @Param("firstDate") LocalDateTime firstDate, @Param("secondDate") LocalDateTime secondDate);

    @Query(nativeQuery = true, value = "SELECT * from shootingplace.sent_email WHERE (sent_at BETWEEN (:firstDate) AND (:secondDate)) order by sent_at")
    List<SentEmail> getEmailSentBetween(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

}
