package com.azure.security;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * @CurrentUserId 이 붙은 파라미터에 현재 로그인한 사용자 ID를 주입.
 * - Long, long, Optional<Long> 지원
 */
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private static final boolean OPTIONAL_PRESENT =
            ClassUtils.isPresent("java.util.Optional", CurrentUserIdArgumentResolver.class.getClassLoader());

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && (Long.class.equals(parameter.getParameterType())
                    || long.class.equals(parameter.getParameterType())
                    || (OPTIONAL_PRESENT && Optional.class.equals(parameter.getParameterType())));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) {

        CurrentUserId ann = parameter.getParameterAnnotation(CurrentUserId.class);
        boolean required = (ann == null) || ann.required();

        Long userId = SecurityUtil.getCurrentUserId();

        // Optional<Long>
        if (OPTIONAL_PRESENT && Optional.class.equals(parameter.getParameterType())) {
            if (userId == null) {
                if (required) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
                return Optional.empty();
            }
            return Optional.of(userId);
        }

        // primitive long
        if (long.class.equals(parameter.getParameterType())) {
            if (userId == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            return userId.longValue();
        }

        // Long
        if (userId == null) {
            if (required) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            return null;
        }
        return userId;
    }
}
