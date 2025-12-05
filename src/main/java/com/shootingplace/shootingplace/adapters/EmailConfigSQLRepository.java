package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.email.EmailConfig;
import com.shootingplace.shootingplace.email.EmailConfigRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface EmailConfigSQLRepository extends EmailConfigRepository, JpaRepository<EmailConfig, String> {

}
