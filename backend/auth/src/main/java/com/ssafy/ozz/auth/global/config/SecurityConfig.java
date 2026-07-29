package com.ssafy.ozz.auth.global.config;

import com.ssafy.ozz.auth.global.filter.GuestLoginFilter;
import com.ssafy.ozz.auth.global.handler.CustomSuccessHandler;
import com.ssafy.ozz.auth.application.port.out.UserAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomSuccessHandler customSuccessHandler;
    private final UserAccountPort userAccountPort;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // cors설정
        http
                .cors(AbstractHttpConfigurer::disable);
//                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
//                    @Override
//                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
//
//                        CorsConfiguration configuration = new CorsConfiguration();
//
//                        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://i11a804.p.ssafy.io:3000"));
//                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                        configuration.setAllowedHeaders(Collections.singletonList("*"));
//                        configuration.setAllowCredentials(true);
//                        configuration.setMaxAge(3600L);
//
//                        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
//
//                        return configuration;
//                    }
//                }));
        //csrf -> jwt로 대체할 것이므로 disable
        http
                .csrf(AbstractHttpConfigurer::disable);
        // form login은 데모 로그인으로 대체
        http
                .formLogin(AbstractHttpConfigurer::disable);
        //HTTP Basic 인증 방식 disable
        http
                .httpBasic(AbstractHttpConfigurer::disable);
        http.exceptionHandling(e->e
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**"))
        );
        // 경로별 인가 작업
//        http
//                .authorizeHttpRequests((auth) -> auth
//                        .requestMatchers("/").permitAll() // 루트경로
//                        // Swagger 관련 경로 허용
//                        .requestMatchers("/swagger-ui/**").permitAll()
//                        .requestMatchers("/v3/api-docs/**").permitAll()
//                        .requestMatchers("/swagger-resources/**").permitAll()
//                        .requestMatchers("/webjars/**").permitAll()
//                        .anyRequest().authenticated()); // 로그인한 사용자가 접근 가능

        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 게스트 로그인 필터 추가
        http.addFilterBefore(new GuestLoginFilter(userAccountPort, customSuccessHandler), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
