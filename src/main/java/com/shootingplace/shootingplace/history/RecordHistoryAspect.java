package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.security.UserAuthService;
import com.shootingplace.shootingplace.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RecordHistoryAspect {

    private final ChangeHistoryService historyService;
    private final UserAuthService userAuthService;

    @AfterReturning("@annotation(recordHistory)")
    public void record(
            JoinPoint joinPoint,
            RecordHistory recordHistory
    ) {
        Object[] args = joinPoint.getArgs();

        String pinCode = extractPinCode(args);
        UserEntity user = userAuthService.getAuthenticatedUser(pinCode);

        String belongsTo = resolveEntityUuid(
                recordHistory,
                args
        );

        historyService.record(
                user,
                recordHistory.action(),
                belongsTo
        );
    }

    private String extractPinCode(Object[] args) {
        return Arrays.stream(args)
                .filter(a -> a instanceof String)
                .map(String.class::cast)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Brak pinCode w metodzie")
                );
    }

    private String resolveEntityUuid(
            RecordHistory recordHistory,
            Object[] args
    ) {
        if (recordHistory.entityArgIndex() < 0) {
            return "N/A";
        }
        Object arg = args[recordHistory.entityArgIndex()];
        return arg != null ? arg.toString() : "N/A";
    }
}

