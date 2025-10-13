package com.azure.config;

import com.azure.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService uds;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            HandlerMappingIntrospector introspector
    ) throws Exception {

        // 경로 매칭 빌더
        var mvc = new MvcRequestMatcher.Builder(introspector);

        // --- 보호할 엔드포인트들 ---
        var apiMatcher        = mvc.pattern("/api/**");            // REST API (예: /api/calendar/**)
        var meMatcher         = mvc.pattern("/me/**");             // 내 정보
        var projectsMatcher   = mvc.pattern("/projects/**");       // 프로젝트(정적 prefix)

        // 필요 시 사용하는 루트 정적 prefix
        var tasksMatcher      = mvc.pattern("/tasks/**");
        var plansMatcher      = mvc.pattern("/plans/**");
        var roomsMatcher      = mvc.pattern("/rooms/**");
        // var calendarsMatcher  = mvc.pattern("/calendars/**");    // 실제 라우트 없으면 제거
        var messagesMatcher   = mvc.pattern("/messages/**");
        var meetingsMatcher   = mvc.pattern("/meetings/**");

        // 동적 prefix (실제로 /{projectId}/... 라우트가 있으면 유지)
        var dynamicTasks      = mvc.pattern("/{projectId:\\d+}/tasks/**");
        var dynamicPlans      = mvc.pattern("/{projectId:\\d+}/plans/**");
        var dynamicRooms      = mvc.pattern("/{projectId:\\d+}/rooms/**");
        var dynamicFiles      = mvc.pattern("/{projectId:\\d+}/files/**");
        var dynamicCards      = mvc.pattern("/{projectId:\\d+}/card/**");
        var dynamicGantt      = mvc.pattern("/{projectId:\\d+}/gantt/**");
        var dynamicChart      = mvc.pattern("/{projectId:\\d+}/chart/**");
        var dynamicCalendar   = mvc.pattern("/{projectId:\\d+}/calendar/**");

        http
            // 개발 중: CSRF 비활성화(운영에선 토큰 고려)
            .csrf(csrf -> csrf.disable())

            // URL 접근 제어
            .authorizeHttpRequests(auth -> auth
                // 공개 리소스만 permitAll
                .requestMatchers(
                    mvc.pattern("/login"),
                    mvc.pattern("/css/**"),
                    mvc.pattern("/js/**"),
                    mvc.pattern("/images/**"),
                    mvc.pattern("/webjars/**"),
                    mvc.pattern("/favicon.ico")
                ).permitAll()

                // 나머지 보호 구간은 로그인 필요
                .requestMatchers(
                    apiMatcher, meMatcher, projectsMatcher,
                    tasksMatcher, plansMatcher, roomsMatcher,
                    messagesMatcher, meetingsMatcher,
                    dynamicTasks, dynamicPlans, dynamicRooms,
                    dynamicFiles, dynamicCards, dynamicGantt,
                    dynamicChart, dynamicCalendar
                ).authenticated()

                // 그 밖의 모든 요청 허용(필요 시 더 엄격히 조이세요)
                .anyRequest().permitAll() 
            )

            // 인증 안 된 접근은 401 (API/보호구간)
            .exceptionHandling(ex -> {
                var entry = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
                ex.defaultAuthenticationEntryPointFor(entry, apiMatcher);
                ex.defaultAuthenticationEntryPointFor(entry, meMatcher);
                ex.defaultAuthenticationEntryPointFor(entry, projectsMatcher);

                ex.defaultAuthenticationEntryPointFor(entry, tasksMatcher);
                ex.defaultAuthenticationEntryPointFor(entry, plansMatcher);
                ex.defaultAuthenticationEntryPointFor(entry, roomsMatcher);
                // ex.defaultAuthenticationEntryPointFor(entry, calendarsMatcher);
                ex.defaultAuthenticationEntryPointFor(entry, messagesMatcher);
                ex.defaultAuthenticationEntryPointFor(entry, meetingsMatcher);

                ex.defaultAuthenticationEntryPointFor(entry, dynamicTasks);
                ex.defaultAuthenticationEntryPointFor(entry, dynamicPlans);
                ex.defaultAuthenticationEntryPointFor(entry, dynamicRooms);
                ex.defaultAuthenticationEntryPointFor(entry, dynamicFiles);
                ex.defaultAuthenticationEntryPointFor(entry, dynamicCards);
                ex.defaultAuthenticationEntryPointFor(entry, dynamicGantt);
                ex.defaultAuthenticationEntryPointFor(entry, dynamicChart);
                ex.defaultAuthenticationEntryPointFor(entry, dynamicCalendar);
            })

            // 폼 로그인
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("userId")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true) // 실제 존재하는 경로로
                .failureUrl("/login?error")
                .permitAll()
            )

            // 로그아웃
            .logout(lo -> lo
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )

            // 우리 UDS 사용
            .userDetailsService(uds);

        return http.build();
    }
}
