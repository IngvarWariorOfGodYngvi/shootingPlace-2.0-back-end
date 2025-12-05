package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.email.EmailSendList;
import com.shootingplace.shootingplace.email.EmailSendListRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendSQLRepository extends EmailSendListRepository, JpaRepository<EmailSendList, String> {
}
