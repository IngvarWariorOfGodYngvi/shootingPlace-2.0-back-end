package com.shootingplace.shootingplace.exceptions.exceptionHandlers;

import com.shootingplace.shootingplace.exceptions.NoPersonToAmmunitionException;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionsHandler {
    private final Logger LOG = LogManager.getLogger(getClass());


    @ExceptionHandler(value = MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingPathVariableException(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(Exception ex) {
        LOG.error("Wprowadzono nieprawidłowe dane");
        return "Wprowadzono nieprawidłowe dane";
    }
    @ExceptionHandler(value = NoPersonToAmmunitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleNoPersonToAmmunitionException(NoPersonToAmmunitionException ex) {
        LOG.error("Wprowadź osobę by wydać amunicję.");
        return "Wprowadź osobę by wydać amunicję.";
    }
    @ExceptionHandler(value = NoUserPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleNoPermissionException(NoUserPermissionException ex) {
        LOG.error("Brak uprawnień.");
        return "Brak uprawnień.";
    }

}