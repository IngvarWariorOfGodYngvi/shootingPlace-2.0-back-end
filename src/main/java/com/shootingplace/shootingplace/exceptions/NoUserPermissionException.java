package com.shootingplace.shootingplace.exceptions;

public class NoUserPermissionException extends Exception {

    public NoUserPermissionException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public NoUserPermissionException() {
        super();
    }

    public NoUserPermissionException(String message) {
        super(message);
    }
}
