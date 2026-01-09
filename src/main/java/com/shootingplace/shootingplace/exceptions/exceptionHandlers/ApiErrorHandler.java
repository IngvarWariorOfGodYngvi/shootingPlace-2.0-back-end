package com.shootingplace.shootingplace.exceptions.exceptionHandlers;

import com.shootingplace.shootingplace.exceptions.ApiError;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class ApiErrorHandler {

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
}

