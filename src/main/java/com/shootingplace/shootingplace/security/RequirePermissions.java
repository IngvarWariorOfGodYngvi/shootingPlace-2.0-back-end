package com.shootingplace.shootingplace.security;

import com.shootingplace.shootingplace.enums.UserSubType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermissions {

    UserSubType[] value();
    boolean requireWork() default true;
}
