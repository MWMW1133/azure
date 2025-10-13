package com.azure.security;

import java.lang.annotation.*;

/**
 * 컨트롤러 핸들러 메서드 파라미터에
 * 로그인한 사용자의 user_id(Long)를 주입하기 위한 어노테이션.
 *
 * 예) public List<?> list(@CurrentUserId Long userId) { ... }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId {
    /**
     * 선택적으로 "필수 여부"를 지정.
     * required=true 이면 미인증 시 401을 던짐.
     * required=false 이면 미인증 시 null(래퍼 타입 Long일 때만) 또는 Optional.empty()로 주입.
     */
    boolean required() default true;
}
