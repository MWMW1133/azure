package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.user.User;
import com.azure.repository.OrganizationRepository;
import com.azure.repository.UserRepository;
import com.azure.service.UserService;
import com.azure.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 도메인 서비스 구현.
 * - 클래스 단위 @Transactional: 쓰기 작업은 기본 트랜잭션, 조회는 readOnly 최적화
 * - 예외 정책: 엔티티 미존재 시 NotFoundException
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    /** 조회 전용 트랜잭션: 더 가볍고, 실수로 flush되지 않도록 보호. */
    @Override @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override @Transactional(readOnly = true)
    public List<User> listByOrganization(Long organizationId) {
        return userRepository.findByOrganizationId(organizationId);
    }

    @Override
    public User create(Long organizationId, String passwordHash, String name, String avatarUrl, Boolean isActive) {
        // FK 무결성 확인
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow();

        // 엔티티 조립
        User u = new User();
        u.setPasswordHash(passwordHash);
        u.setName(name);
        u.setAvatarUrl(avatarUrl);
        u.setIsActive(isActive);
        u.setOrganization(org);

        return userRepository.save(u);
    }

    @Override
    public User update(Long userId, String name, String avatarUrl, Boolean isActive) {
        User u = get(userId);
        if (name != null) u.setName(name);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        if (isActive != null) u.setIsActive(isActive);
        return userRepository.save(u);
    }

    @Override
    public void delete(Long userId) {
        // 소프트 삭제로 바꾸고 싶다면 isActive=false로 전환하는 정책을 사용
        userRepository.delete(get(userId));
    }
}
