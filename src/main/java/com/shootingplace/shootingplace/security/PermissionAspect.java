package com.shootingplace.shootingplace.security;

import com.shootingplace.shootingplace.users.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final UserAuthService userAuthService;

    @Before("@annotation(com.shootingplace.shootingplace.security.RequirePermissions)")
    public void checkPermissions(JoinPoint joinPoint) {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermissions rp = method.getAnnotation(RequirePermissions.class);

        String pinCode = extractPin(joinPoint.getArgs());
        if (pinCode == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Brak parametru pinCode"
            );
        }

        try {
            UserEntity user = userAuthService.authenticate(pinCode);
            userAuthService.hasAnyPermission(
                    user,
                    rp.value(),
                    rp.requireWork()
            );
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Błędny PIN");
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    private String extractPin(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String s && s.length() >= 4) {
                return s;
            }
        }
        return null;
    }
}
