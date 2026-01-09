package com.shootingplace.shootingplace.exceptions;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        int status,
        String error,
        String code,
        String message,
        Map<String, Object> details,
        String path,
        Instant timestamp
) {

    public static ApiError notFound(
            String entity,
            String identifier,
            String path
    ) {
        return new ApiError(
                404,
                "NOT_FOUND",
                "ENTITY_NOT_FOUND",
                "Nie znaleziono obiektu",
                Map.of(
                        "entity", entity,
                        "identifier", identifier
                ),
                path,
                Instant.now()
        );
    }
}

