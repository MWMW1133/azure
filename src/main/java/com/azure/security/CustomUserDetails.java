package com.azure.security;

import com.azure.model.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    // 생성자  
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // User 엔티티의 ID를 반환하는 메서드 추가
    public Long getId() {
        return user.getId(); // PK 반환
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한이 따로 없으면 빈 리스트
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash(); // DB 비밀번호
    }

    @Override
    public String getUsername() {
        return user.getLoginId(); // 로그인 ID
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
    return user.getWorkStatus() == User.WorkStatus.WORKING;
    }

}