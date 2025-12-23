package com.shootingplace.shootingplace.strategies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface EmailStrategy {

    Logger LOG = LogManager.getLogger(EmailStrategy.class);

    default String getBcc() {
        String email = "automat@smartstrzelnica.pl";
        logBcc(email);
        return email;
    }

    default String getDatesString() {
        return "";
    }
    default  String getAHrefSite() {
        return "";
    }

    default void logBcc(String email) {
        LOG.info("ukryta kopia do: {}", email);
    }
}
