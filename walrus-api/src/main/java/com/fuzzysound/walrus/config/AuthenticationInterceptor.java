package com.fuzzysound.walrus.config;

import com.fuzzysound.walrus.provider.AuthenticationProvider;
import com.fuzzysound.walrus.common.exception.AuthenticationFailedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final AuthenticationProvider authenticationProvider;
    private static final String USERNAME_HEADER = "X-username";
    private static final String PASSWORD_HEADER = "X-password";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String username = request.getHeader(USERNAME_HEADER);
        String password = request.getHeader(PASSWORD_HEADER);
        if (username != null && password != null) {
            authenticationProvider.authenticate(username, password);
            return true;
        } else {
            throw new AuthenticationFailedException(
                    String.format("Header %s and %s must be provided.", USERNAME_HEADER, PASSWORD_HEADER)
            );
        }
    }

}
