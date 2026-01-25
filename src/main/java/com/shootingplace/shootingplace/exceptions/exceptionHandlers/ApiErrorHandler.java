package com.shootingplace.shootingplace.exceptions.exceptionHandlers;

import com.shootingplace.shootingplace.exceptions.ApiError;
import com.shootingplace.shootingplace.exceptions.NoPersonToAmmunitionException;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.exceptions.license.LicenseProlongationTooEarlyException;
import com.shootingplace.shootingplace.exceptions.pinExceptions.InvalidPinException;
import com.shootingplace.shootingplace.exceptions.pinExceptions.PinTooShortException;
import com.shootingplace.shootingplace.exceptions.portal.PortalExportException;
import com.shootingplace.shootingplace.exceptions.soz.SozExportException;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class ApiErrorHandler {

    private final Logger LOG = LogManager.getLogger(getClass());

    /* =========================
       400 BAD REQUEST
       ========================= */

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiError> handleMissingPathVariable(
            MissingPathVariableException ex,
            HttpServletRequest request
    ) {
        return badRequest(
                "MISSING_PATH_VARIABLE",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(PinTooShortException.class)
    public ResponseEntity<ApiError> handlePinTooShort(HttpServletRequest request) {
        return badRequest(
                "PIN_TOO_SHORT",
                "PIN za krótki",
                request
        );
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ApiError> handleInvalidPin(HttpServletRequest request) {
        return badRequest(
                "INVALID_PIN",
                "Błędny PIN",
                request
        );
    }

    @ExceptionHandler(NoOneAtWorkException.class)
    public ResponseEntity<ApiError> handleNoOneAtWork(HttpServletRequest request) {
        return badRequest(
                "NO_ONE_AT_WORK",
                "Najpierw zarejestruj pobyt",
                request
        );
    }

    @ExceptionHandler(NoPersonToAmmunitionException.class)
    public ResponseEntity<ApiError> handleNoPersonToAmmunition(HttpServletRequest request) {
        LOG.error("Wprowadź osobę by wydać amunicję.");
        return badRequest(
                "NO_PERSON_FOR_AMMUNITION",
                "Wprowadź osobę by wydać amunicję",
                request
        );
    }

    /* =========================
       403 FORBIDDEN
       ========================= */

    @ExceptionHandler(UserNotAtWorkException.class)
    public ResponseEntity<ApiError> handleUserNotAtWork(HttpServletRequest request) {
        return forbidden(
                "USER_NOT_AT_WORK",
                "Najpierw zarejestruj pobyt",
                request
        );
    }

    @ExceptionHandler(MissingPermissionException.class)
    public ResponseEntity<ApiError> handleMissingPermission(HttpServletRequest request) {
        return forbidden(
                "MISSING_PERMISSION",
                "Brak wymaganych uprawnień",
                request
        );
    }

    @ExceptionHandler(UserNotAtWorkAndNoPermissionException.class)
    public ResponseEntity<ApiError> handleUserNotAtWorkAndNoPermission(HttpServletRequest request) {
        return forbidden(
                "USER_NOT_AT_WORK_AND_NO_PERMISSION",
                "Użytkownik nie jest odbity do pracy i nie ma uprawnień",
                request
        );
    }

    @ExceptionHandler(LicenseProlongationTooEarlyException.class)
    public ResponseEntity<ApiError> handleLicenseTooEarly(
            HttpServletRequest request
    ) {
        return forbidden(
                "LICENSE_PROLONGATION_TOO_EARLY",
                "Nie można przedłużyć licencji - należy poczekać do 1 listopada",
                request
        );
    }

    /* =========================
       404 NOT FOUND
       ========================= */

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

    /* =========================
       HTTP STATUS EXCEPTION
       ========================= */

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ApiError(
                        ex.getStatusCode().value(),
                        ex.getStatusCode().toString(),
                        "HTTP_ERROR",
                        ex.getReason(),
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    /* =========================
       500 FALLBACK
       ========================= */

    //    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> handleUnexpected(
//            Exception ex,
//            HttpServletRequest request
//    ) {
//        LOG.error("Nieobsłużony wyjątek", ex);
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ApiError(
//                        500,
//                        "INTERNAL_SERVER_ERROR",
//                        "UNEXPECTED_ERROR",
//                        "Wystąpił nieoczekiwany błąd",
//                        null,
//                        request.getRequestURI(),
//                        Instant.now()
//                ));
//    }
    @ExceptionHandler(SozExportException.class)
    public ResponseEntity<ApiError> handleSozExport(
            SozExportException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError(
                        502,
                        "BAD_GATEWAY",
                        "SOZ_EXPORT_ERROR",
                        ex.getMessage(),
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(PortalExportException.class)
    public ResponseEntity<ApiError> handlePortalExport(
            PortalExportException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiError(
                        ex.getStatus().value(),
                        ex.getStatus().name(),
                        ex.getCode(),
                        ex.getMessage(),
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError(
                        502,
                        "BAD_GATEWAY",
                        "PORTAL_EXPORT_ERROR",
                        ex.getMessage(),
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    /* =========================
       HELPERS
       ========================= */

    private ResponseEntity<ApiError> badRequest(
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        400,
                        "BAD_REQUEST",
                        code,
                        message,
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }

    private ResponseEntity<ApiError> forbidden(
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        403,
                        "FORBIDDEN",
                        code,
                        message,
                        null,
                        request.getRequestURI(),
                        Instant.now()
                ));
    }
}
