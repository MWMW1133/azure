package com.azure.security;

import com.azure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        var user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found: " + loginId));
        return new CustomUserDetails(user);
    }
}
