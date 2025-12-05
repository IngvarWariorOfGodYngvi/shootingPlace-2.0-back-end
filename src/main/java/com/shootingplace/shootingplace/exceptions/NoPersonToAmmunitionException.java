package com.shootingplace.shootingplace.exceptions;

public class NoPersonToAmmunitionException extends Exception {
    public NoPersonToAmmunitionException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public NoPersonToAmmunitionException() {
        super();
    }

    public NoPersonToAmmunitionException(String message) {
        super(message);
    }
}
