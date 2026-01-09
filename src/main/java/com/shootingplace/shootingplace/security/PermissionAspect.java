package com.shootingplace.shootingplace.security;

import com.shootingplace.shootingplace.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final UserAuthService userAuthService;
    private final UserAuthContext userAuthContext;

    @Before("@annotation(com.shootingplace.shootingplace.security.RequirePermissions)")
    public void checkPermissions(JoinPoint joinPoint) {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermissions rp = method.getAnnotation(RequirePermissions.class);

        String pinCode = extractPinFromHeader();
        if (pinCode == null || pinCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brak nagłówka X-OPERATOR-PIN");
        }

        UserEntity user = userAuthService.authenticate(pinCode);
        userAuthService.hasAnyPermission(user, rp.value(), rp.requireWork());
        userAuthContext.set(user);
    }

    private String extractPinFromHeader() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attrs != null ? attrs.getRequest().getHeader("X-OPERATOR-PIN") : null;
    }
}

