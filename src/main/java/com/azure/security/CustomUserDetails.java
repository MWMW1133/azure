package com.azure.security;

import com.azure.model.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    /** 조직이 없을 수 있으므로 NPE 방지 */
    public Long getOrganizationId() {
        return (user.getOrganization() != null) ? user.getOrganization().getId() : 0L;
        // 정책상 null이 더 낫다면 Long 반환으로 바꾸고 null 리턴하세요.
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getLoginId(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getWorkStatus() == User.WorkStatus.WORKING; }
}
