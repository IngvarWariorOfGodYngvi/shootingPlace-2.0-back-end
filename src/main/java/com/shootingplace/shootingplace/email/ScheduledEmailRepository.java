package com.shootingplace.shootingplace.email;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledEmailRepository {
    List<ScheduledEmail> findAllByScheduledForBefore(LocalDateTime now);

    ScheduledEmail save(ScheduledEmail scheduledEmail);

    ScheduledEmail getByUuid(String uuid);

    void delete(ScheduledEmail e);
    @Query(nativeQuery = true, value = "SELECT * from shootingplace.scheduled_email WHERE memberuuid = (:memberUUID) AND mail_type = (:mailType) AND DATE(scheduled_for) = CURDATE()")
    List<ScheduledEmail> findTodayByMemberAndMailType(@Param("memberUUID") String memberUUID, @Param("mailType") String mailType);

    List<ScheduledEmail> findAll();
}
