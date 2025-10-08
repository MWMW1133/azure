package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.OrganizationMember;
import com.azure.model.OrganizationMemberId;
import com.azure.model.enums.OrganizationRole;
import com.azure.model.user.User;
import com.azure.repository.OrganizationMemberRepository;
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
    private final OrganizationMemberRepository organizationMemberRepository;

    /** 조회 전용 트랜잭션 */
    @Override @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findByIdFetchOrganization(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Override @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
 
    @Override @Transactional(readOnly = true) 
    public List<User> listByOrganization(Long organizationId) {
        // 장기휴가, 연차, 반차자는 제외
        return userRepository.findByOrganizationIdAndWorkStatus(organizationId, User.WorkStatus.WORKING);
    }


    @Override
    public User create(Long organizationId, String loginId, String passwordHash,
                       String name, String avatarUrl, User.WorkStatus workStatus,
                       boolean isAdminSignup, String companyName) {

        System.out.println("[DEBUG] userService.create() 실행됨");

        if (userRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + loginId);
        }

        // 1) 유저만 먼저 저장
        User u = new User();
        u.setLoginId(loginId);
        u.setPasswordHash(passwordHash);
        u.setName(name);
        u.setAvatarUrl(avatarUrl);
        u.setWorkStatus(workStatus != null ? workStatus : User.WorkStatus.WORKING);
        userRepository.save(u);
        userRepository.flush();

        // 2) 관리자 가입: 회사명으로 조직 만들거나 재사용하고 MANAGER로 조인
        if (isAdminSignup && companyName != null && !companyName.isBlank()) {
            Organization org = organizationRepository.findByName(companyName)
                    .orElseGet(() -> {
                        Organization o = new Organization();
                        o.setName(companyName);
                        o.setUser(u);
                        return organizationRepository.save(o);
                    });

            u.setOrganization(org);
            userRepository.save(u);

            OrganizationMember member = new OrganizationMember();
            member.setId(new OrganizationMemberId(org.getId(), u.getId()));
            member.setOrganization(org);
            member.setUser(u);
            member.setRole(OrganizationRole.MANAGER);
            organizationMemberRepository.save(member);

            System.out.println("[DEBUG] 관리자 가입: orgId=" + org.getId() + ", userId=" + u.getId());
            return u;
        }

        // 3) 초대 가입: orgId가 들어오면 MEMBER로 조인
        if (organizationId != null) {
            Organization org = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new NotFoundException("Organization not found: " + organizationId));

            OrganizationMember member = new OrganizationMember();
            member.setId(new OrganizationMemberId(org.getId(), u.getId()));
            member.setOrganization(org);
            member.setUser(u);
            member.setRole(OrganizationRole.MEMBER);
            organizationMemberRepository.save(member);

            System.out.println("[DEBUG] 초대 가입: orgId=" + org.getId() + ", userId=" + u.getId());
            return u;
        }

        // 4) 일반 가입: 아직 어떤 조직에도 속하지 않음 (나중에 초대로 조인)
        System.out.println("[DEBUG] 일반 가입: 조직 미소속 userId=" + u.getId());
        return u;
    }


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

    @Override
    @Transactional
    public Optional<OrganizationMember> findMembershipByUserId(Long userId) {
        return organizationMemberRepository.findByUserIdFetchOrganization(userId)
                .stream().findFirst();
    }

    // 조직 미가입자 조회
    @Override
    @Transactional(readOnly = true)
    public List<User> searchInvitableUsers(String keyword) {
        return userRepository.searchInvitableUsers(keyword);
    }

}
