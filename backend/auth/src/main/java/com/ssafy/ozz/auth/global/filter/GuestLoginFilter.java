package com.ssafy.ozz.auth.global.filter;

import com.ssafy.ozz.auth.global.util.UserClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class GuestLoginFilter extends OncePerRequestFilter {

    private final UserClient userClient;
    private final AuthenticationSuccessHandler successHandler;

    public GuestLoginFilter(UserClient userClient, AuthenticationSuccessHandler successHandler) {
        this.userClient = userClient;
        this.successHandler = successHandler;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 게스트 로그인 요청 처리
        if ("/login/guest".equals(request.getRequestURI())) {
            long guestUserId = userClient.createGuest().getBody();
            // 1. 게스트 계정 정의
            UsernamePasswordAuthenticationToken guestAuth = new UsernamePasswordAuthenticationToken(
                    guestUserId, // 게스트 사용자 이름
                    null // 비밀번호는 없음
            );

            // 2. SecurityContext에 인증 설정
            SecurityContextHolder.getContext().setAuthentication(guestAuth);

            // 3. 인증 성공 핸들러 호출
            successHandler.onAuthenticationSuccess(request, response, guestAuth);

            return;
        }

        // 다른 요청은 기본 체인을 계속 처리
        chain.doFilter(request, response);
    }
}
