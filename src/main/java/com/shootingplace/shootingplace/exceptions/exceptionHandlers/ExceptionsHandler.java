package com.shootingplace.shootingplace.exceptions.exceptionHandlers;

import com.shootingplace.shootingplace.exceptions.ApiError;
import com.shootingplace.shootingplace.exceptions.NoPersonToAmmunitionException;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.exceptions.pinExceptions.InvalidPinException;
import com.shootingplace.shootingplace.exceptions.pinExceptions.PinTooShortException;
import com.shootingplace.shootingplace.exceptions.userStateExceptions.MissingPermissionException;
import com.shootingplace.shootingplace.exceptions.userStateExceptions.UserNotAtWorkAndNoPermissionException;
import com.shootingplace.shootingplace.exceptions.userStateExceptions.UserNotAtWorkException;
import com.shootingplace.shootingplace.exceptions.workingTimeExceptions.NoOneAtWorkException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ExceptionsHandler {
    private final Logger LOG = LogManager.getLogger(getClass());


    @ExceptionHandler(value = MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingPathVariableException(Exception ex) {
        return ex.getMessage();
    }
    @ExceptionHandler(value = NoPersonToAmmunitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleNoPersonToAmmunitionException() {
        LOG.error("Wprowadź osobę by wydać amunicję.");
        return "Wprowadź osobę by wydać amunicję.";
    }
    @ExceptionHandler(PinTooShortException.class)
    public ResponseEntity<String> pinTooShort() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("PIN za krótki");
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<String> invalidPin() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Błędny PIN");
    }

    @ExceptionHandler(UserNotAtWorkException.class)
    public ResponseEntity<String> notAtWork() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Najpierw zarejestruj pobyt");
    }

    @ExceptionHandler(MissingPermissionException.class)
    public ResponseEntity<String> noPermission() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Brak wymaganych uprawnień");
    }
    @ExceptionHandler(NoOneAtWorkException.class)
    public ResponseEntity<ApiError> handleNoOneAtWork(
            NoOneAtWorkException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        400,
                        "BAD_REQUEST",
                        "NO_ONE_AT_WORK",
                        "Najpierw zarejestruj pobyt",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(UserNotAtWorkAndNoPermissionException.class)
    public ResponseEntity<String> notAtWorkAndNoPermission() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Użytkownik nie jest odbity do pracy i nie ma uprawnień");
    }

    @ExceptionHandler(DomainNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(DomainNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.notFound(
                        ex.getEntity(),
                        ex.getIdentifier(),
                        ex.getMessage()
                ));
    }

}