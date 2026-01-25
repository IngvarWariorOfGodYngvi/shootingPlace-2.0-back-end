package com.shootingplace.shootingplace.soz;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class SozSession {

    private final MultiValueMap<String, String> cookies = new LinkedMultiValueMap<>();
    private boolean loggedIn;

    public MultiValueMap<String, String> cookies() {
        return cookies;
    }

    public boolean isValid() {
        return loggedIn && cookies.containsKey(".ASPXAUTH");
    }

    public void markLoggedIn() {
        this.loggedIn = true;
    }

    public void clear() {
        cookies.clear();
        loggedIn = false;
    }
}



