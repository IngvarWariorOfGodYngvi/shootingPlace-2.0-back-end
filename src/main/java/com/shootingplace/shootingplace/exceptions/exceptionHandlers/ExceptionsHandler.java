package com.shootingplace.shootingplace.exceptions.exceptionHandlers;

import com.shootingplace.shootingplace.exceptions.ApiError;
import com.shootingplace.shootingplace.exceptions.NoPersonToAmmunitionException;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.exceptions.license.LicenseProlongationTooEarlyException;
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
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ExceptionsHandler {

    private final Logger LOG = LogManager.getLogger(getClass());

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiError> handleMissingPathVariable(
            MissingPathVariableException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        400,
                        "BAD_REQUEST",
                        "MISSING_PATH_VARIABLE",
                        ex.getMessage(),
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(NoPersonToAmmunitionException.class)
    public ResponseEntity<ApiError> handleNoPersonToAmmunition(
            HttpServletRequest request
    ) {
        LOG.error("Wprowadź osobę by wydać amunicję.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        400,
                        "BAD_REQUEST",
                        "NO_PERSON_FOR_AMMUNITION",
                        "Wprowadź osobę by wydać amunicję",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(PinTooShortException.class)
    public ResponseEntity<ApiError> handlePinTooShort(
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        400,
                        "BAD_REQUEST",
                        "PIN_TOO_SHORT",
                        "PIN za krótki",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ApiError> handleInvalidPin(
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        400,
                        "BAD_REQUEST",
                        "INVALID_PIN",
                        "Błędny PIN",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(UserNotAtWorkException.class)
    public ResponseEntity<ApiError> handleUserNotAtWork(
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        403,
                        "FORBIDDEN",
                        "USER_NOT_AT_WORK",
                        "Najpierw zarejestruj pobyt",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(MissingPermissionException.class)
    public ResponseEntity<ApiError> handleMissingPermission(
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        403,
                        "FORBIDDEN",
                        "MISSING_PERMISSION",
                        "Brak wymaganych uprawnień",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(UserNotAtWorkAndNoPermissionException.class)
    public ResponseEntity<ApiError> handleUserNotAtWorkAndNoPermission(
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        403,
                        "FORBIDDEN",
                        "USER_NOT_AT_WORK_AND_NO_PERMISSION",
                        "Użytkownik nie jest odbity do pracy i nie ma uprawnień",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(NoOneAtWorkException.class)
    public ResponseEntity<ApiError> handleNoOneAtWork(
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

    @ExceptionHandler(DomainNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            DomainNotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.notFound(
                        ex.getEntity(),
                        ex.getIdentifier(),
                        request.getRequestURI()
                ));
    }
    @ExceptionHandler(LicenseProlongationTooEarlyException.class)
    public ResponseEntity<ApiError> handleLicenseProlongationTooEarly(
            LicenseProlongationTooEarlyException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        403,
                        "FORBIDDEN",
                        "LICENSE_PROLONGATION_TOO_EARLY",
                        "Nie można przedłużyć licencji - należy poczekać do 1 listopada",
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }
}
