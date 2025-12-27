package com.shootingplace.shootingplace.security;

import com.shootingplace.shootingplace.users.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAuthContext {

    private static final ThreadLocal<UserEntity> CURRENT = new ThreadLocal<>();

    public void set(UserEntity user) {
        CURRENT.set(user);
    }

    public UserEntity get() {
        return CURRENT.get();
    }

    public boolean hasUser() {
        return CURRENT.get() != null;
    }

    public void clear() {
        CURRENT.remove();
    }
}

