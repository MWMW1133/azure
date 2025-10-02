package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.user.User;
import com.azure.model.user.User.WorkStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 조직 ID로 사용자 목록 조회
    List<User> findByOrganizationIdAndWorkStatus(Long organizationId, WorkStatus workStatus);
    // 로그인 ID로 사용자 조회
    Optional<User> findByLoginId(String loginId);
    // 로그인 ID 존재 여부 확인
    boolean existsByLoginId(String loginId);
}
