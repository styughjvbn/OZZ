package com.ssafy.ozz.monolith.web;

import com.ssafy.ozz.auth.global.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String[] PUBLIC_PATHS = {
            "/login/guest", "/docs", "/v3/api-docs", "/swagger", "/actuator"
    };

    private final JWTUtil jwtUtil;

    @Value("${INTERNAL_API_TOKEN:}")
    private String internalApiToken;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        String suppliedInternalToken = request.getHeader("X-Internal-Token");
        if (!internalApiToken.isBlank()
                && internalApiToken.equals(suppliedInternalToken)
                && (path.startsWith("/api/clothes") || path.startsWith("/api/categories"))) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String token = authorization.substring(7);
            String userId = jwtUtil.getUserId(token);
            if (userId == null || jwtUtil.isExpired(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String suppliedUserId = request.getHeader(USER_ID_HEADER);
            if (suppliedUserId != null && !suppliedUserId.equals(userId)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            filterChain.doFilter(new UserIdRequest(request, userId), response);
        } catch (Exception exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private static final class UserIdRequest extends HttpServletRequestWrapper {
        private final String userId;

        private UserIdRequest(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            return USER_ID_HEADER.equalsIgnoreCase(name) ? userId : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return USER_ID_HEADER.equalsIgnoreCase(name)
                    ? Collections.enumeration(Collections.singleton(userId))
                    : super.getHeaders(name);
        }
    }
}
