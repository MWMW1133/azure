package com.azure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    /** 보안 설정 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 개발 단계: 전체 CSRF 비활성화
            // (나중에 활성화할 경우, /ws-chat/** 와 /api/messages/** 는 CSRF 예외로 두면 됨)
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // ✅ 앞으로 범위를 좁힐 때를 대비한 명시적 허용 경로
                .requestMatchers(
                    "/ws-chat/**",        // SockJS 핸드셰이크
                    "/topic/**", "/app/**", // STOMP topic/app 프리픽스
                    "/api/messages/**",   // 채팅 과거 조회 REST
                    "/css/**", "/js/**", "/images/**", "/icons/**"
                ).permitAll()

                // 현재는 전체 허용(테스트용). 필요 시 여기만 조이면 됨.
                .anyRequest().permitAll()
            )

            // 폼 로그인/세션 로그인 사용 안 함(팀 설정 유지)
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
