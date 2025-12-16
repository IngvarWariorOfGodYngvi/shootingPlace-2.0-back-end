package com.shootingplace.shootingplace.users;

import org.springframework.stereotype.Component;

@Component
public class UserPinPolicy {

    public void validate(String pin) {
        if (pin == null || pin.length() < 4) {
            throw new IllegalArgumentException("PIN za krótki");
        }

        char[] p = pin.toCharArray();

        if (allSame(p)) {
            throw new IllegalArgumentException("PIN zbyt prosty");
        }
        if (ascending(p) || descending(p)) {
            throw new IllegalArgumentException("PIN zbyt prosty");
        }
    }

    private boolean allSame(char[] p) {
        return p[0] == p[1] && p[1] == p[2] && p[2] == p[3];
    }

    private boolean ascending(char[] p) {
        return p[0]+1==p[1] && p[1]+1==p[2] && p[2]+1==p[3];
    }

    private boolean descending(char[] p) {
        return p[0]-1==p[1] && p[1]-1==p[2] && p[2]-1==p[3];
    }
}
