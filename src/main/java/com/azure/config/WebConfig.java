package com.azure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserSessionInterceptor userSessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userSessionInterceptor)
                .addPathPatterns("/**") // 모든 주소에 인터셉터를 적용
                .excludePathPatterns("/login", "/signup", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error"); // 로그인, 회원가입, 리소스 폴더 등은 제외
    }
}

