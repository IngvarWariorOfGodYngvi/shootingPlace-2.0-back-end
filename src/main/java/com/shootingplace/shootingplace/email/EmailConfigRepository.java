package com.shootingplace.shootingplace.email;

import java.util.List;

public interface EmailConfigRepository {

    EmailConfig save(EmailConfig config);

    List<EmailConfig> findAll();

    EmailConfig getOne(String uuid);
}
