package com.azure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 보안 설정 (개발 모드: 채팅 관련 API 임시 오픈) */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 개발 중에는 CSRF 비활성화 (POST 테스트 편의)
            .csrf(csrf -> csrf.disable())

                // 세션은 필요 시 생성 (JSESSIONID 사용)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // 경로별 권한
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스/공개 페이지
                .requestMatchers(
                    "/", "/home", "/login", "/logout",
                    "/css/**", "/js/**", "/images/**", "/webjars/**"
                ).permitAll()

                // 로그인/회원 관련 API (프로젝트 정책에 맞게 공개 유지)
                .requestMatchers(
                    "/api/users/login", "/api/users/signup"
                ).permitAll()

                // ============================
                // 💬 채팅 관련 API 임시 오픈 (개발용)
                //   - 메시지 목록/저장
                //   - 채널/DM 채널 생성·조회
                //   - DM 리스트용 사용자 조회
                // ============================
                .requestMatchers(
                    "/api/messages/**",
                    "/api/channels/**",
                    "/api/users/**"
                ).permitAll()

                // 그 외 요청
                .anyRequest().permitAll()
            )

            // 내장 로그인폼 사용 안 함(리다이렉트 방지용)
            .formLogin(login -> login.disable())

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );


        return http.build();
    }
}