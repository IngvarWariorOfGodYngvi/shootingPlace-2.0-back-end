package com.shootingplace.shootingplace.exceptions.portal;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PortalExportException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public PortalExportException(
            HttpStatus status,
            String code,
            String message
    ) {
        super(message);
        this.status = status;
        this.code = code;
    }

}

