package com.shootingplace.shootingplace.users;

import org.springframework.stereotype.Component;

@Component
public class UserPinPolicy {

    public void validate(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN musi składać się z 4 cyfr");
        }

        char[] p = pin.toCharArray();

        if (allSame(p) || ascending(p) || descending(p)) {
            throw new IllegalArgumentException("PIN zbyt prosty");
        }
    }

    private boolean allSame(char[] p) {
        return p[0] == p[1] && p[1] == p[2] && p[2] == p[3];
    }

    private boolean ascending(char[] p) {
        return (p[0] - '0') + 1 == (p[1] - '0')
                && (p[1] - '0') + 1 == (p[2] - '0')
                && (p[2] - '0') + 1 == (p[3] - '0');
    }

    private boolean descending(char[] p) {
        return (p[0] - '0') - 1 == (p[1] - '0')
                && (p[1] - '0') - 1 == (p[2] - '0')
                && (p[2] - '0') - 1 == (p[3] - '0');
    }
}
