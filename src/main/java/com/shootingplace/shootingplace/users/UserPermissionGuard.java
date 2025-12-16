package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPermissionGuard {

    public void requireAny(UserEntity user, List<String> required) throws NoUserPermissionException {
        if (user.getUserPermissionsList() == null) {
            throw new NoUserPermissionException();
        }

        boolean ok = user.getUserPermissionsList().stream().anyMatch(required::contains);

        if (!ok) {
            throw new NoUserPermissionException();
        }
    }
}


