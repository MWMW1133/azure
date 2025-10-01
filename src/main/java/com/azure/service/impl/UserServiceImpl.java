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
import java.util.Optional;

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

    /** 조회 전용 트랜잭션 */
    @Override @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
    // 전체 사용자 목록 조회(페이징)
    @Override @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    // 특정 조직에 속한 사용자 목록 조회(휴면/퇴사자 제외)
    @Override @Transactional(readOnly = true) 
    public List<User> listByOrganization(Long organizationId) {
        // 장기휴가, 연차, 반차자는 제외
        return userRepository.findByOrganizationIdAndWorkStatus(organizationId, User.WorkStatus.WORKING);
    }
    // 사용자 생성
    @Override
    public User create(Long organizationId, String loginId, String passwordHash, String name, String avatarUrl, User.WorkStatus workStatus) {
        // 로그인 ID 중복 체크
        if (userRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + loginId);
        }

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + organizationId));

        User u = new User();
        u.setLoginId(loginId);
        u.setPasswordHash(passwordHash);
        u.setName(name);
        u.setAvatarUrl(avatarUrl);
        u.setWorkStatus(workStatus != null ? workStatus : User.WorkStatus.WORKING); // 기본값 WORKING
        u.setOrganization(org);
        return userRepository.save(u);
    }
    // 로그인된 사용자 정보 수정 (비밀번호, 이름, 아바타, 근무 상태)
    @Override
    public User update(String passwordHash, String name, String avatarUrl, User.WorkStatus workStatus) {
        Long currentUserId = com.azure.security.SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        User u = get(currentUserId);
        if (passwordHash != null) u.setPasswordHash(passwordHash);
        if (name != null) u.setName(name);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        if (workStatus != null) u.setWorkStatus(workStatus);

        return userRepository.save(u);
    }
    // 사용자 삭제 (물리 삭제)
    @Override
    public void delete(Long userId) {
        User u = get(userId);
        // 삭제 대신 "휴면/퇴사 상태"로 처리할 수 있음 → 여기서는 물리 삭제
        userRepository.delete(u);
    }
    // 로그인 ID 중복 여부 확인
    @Override
    @Transactional(readOnly = true)
    public boolean existsByLoginId(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }   
    // 로그인 ID로 사용자 조회(없으면 Optional.empty())
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId);
    }
}
