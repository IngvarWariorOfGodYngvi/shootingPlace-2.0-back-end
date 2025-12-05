package com.shootingplace.shootingplace.email;

import java.util.List;

public interface EmailSendListRepository {
    List<EmailSendList> findAll();

    EmailSendList save(EmailSendList emailSendList);
}
