package com.ssafy.ozz.auth.global.handler;

import com.ssafy.ozz.auth.global.service.RefreshService;
import com.ssafy.ozz.auth.global.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof Long userId)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String access = jwtUtil.createJwt("access", userId, 60000000L);
        String refresh = refreshService.createAndSaveRefreshToken(userId);

        response.setHeader("access", access);
        response.setHeader("refresh", refresh);

        Cookie accessCookie = new Cookie("access", access);
        accessCookie.setPath("/");
//        accessCookie.setHttpOnly(true); 클라이언트에서 접근 못하게
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh", refresh);
        refreshCookie.setPath("/");
//        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);

        response.sendRedirect(frontendUrl + "/");
    }
}
