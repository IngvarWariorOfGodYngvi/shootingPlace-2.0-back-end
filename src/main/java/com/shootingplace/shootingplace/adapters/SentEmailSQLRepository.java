package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.email.SentEmail;
import com.shootingplace.shootingplace.email.SentEmailRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface SentEmailSQLRepository extends SentEmailRepository, JpaRepository<SentEmail, String> {
}
