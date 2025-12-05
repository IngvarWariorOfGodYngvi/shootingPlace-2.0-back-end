package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.email.ScheduledEmail;
import com.shootingplace.shootingplace.email.ScheduledEmailRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface ScheduledEmailSQLRepository extends ScheduledEmailRepository, JpaRepository<ScheduledEmail,Long> {
}
