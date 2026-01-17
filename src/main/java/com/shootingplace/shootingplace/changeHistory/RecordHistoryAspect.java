package com.shootingplace.shootingplace.changeHistory;

import com.shootingplace.shootingplace.security.UserAuthContext;
import com.shootingplace.shootingplace.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RecordHistoryAspect {

    private final ChangeHistoryService historyService;
    private final UserAuthContext userAuthContext;

    @AfterReturning("@annotation(recordHistory)")
    public void record(JoinPoint joinPoint, RecordHistory recordHistory) {
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w UserAuthContext – @RecordHistory bez @RequirePermissions?");
        }

        String belongsTo = resolveEntityUuid(recordHistory, joinPoint.getArgs());

        historyService.record(user, recordHistory.action(), belongsTo);
    }

    private String resolveEntityUuid(RecordHistory recordHistory, Object[] args) {
        if (recordHistory.entityArgIndex() < 0) {
            return "N/A";
        }

        Object arg = args[recordHistory.entityArgIndex()];
        return arg != null ? arg.toString() : "N/A";
    }
}
