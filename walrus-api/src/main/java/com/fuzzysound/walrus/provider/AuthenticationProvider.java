package com.fuzzysound.walrus.provider;

import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;


@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
public class AuthenticationProvider {
    private boolean authenticated = false;
    private String username = "";
    private String password = "";

    public void authenticate(String username, String password) {
        this.authenticated = true;
        this.username = username;
        this.password = password;
    }
}
