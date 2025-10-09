package com.azure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.azure.security.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService uds;

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
              .requestMatchers("/api/calendar/**").authenticated()  // 캘린더는 로그인 필요
              .anyRequest().permitAll()
          )
          .formLogin(form -> form
              .loginPage("/login")                 // 로그인 페이지(아래 5번 참고)
              .usernameParameter("userId")        // 폼 필드명: userId
              .passwordParameter("password")
              .defaultSuccessUrl("/calendar", true)
              .failureUrl("/login?error")
              .permitAll()
          )
          .logout(logout -> logout
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login")
              .invalidateHttpSession(true)
              .deleteCookies("JSESSIONID")
          )
          .userDetailsService(uds); // ★ 핵심: 우리가 만든 UDS 사용

        return http.build();
    }
}
