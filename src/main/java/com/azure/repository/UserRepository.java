package com.azure.repository;

import com.azure.model.user.User;
import com.azure.model.user.User.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 조직 ID + 재직 상태로 사용자 목록
    List<User> findByOrganizationIdAndWorkStatus(Long organizationId, WorkStatus workStatus);

    // ✅ 같은 조직 전원(상태 무관) 이름 오름차순
    List<User> findByOrganizationIdOrderByNameAsc(Long organizationId);

    // 로그인 ID로 사용자 단건 조회 (Security principal.getName()과 매핑)
    Optional<User> findByLoginId(String loginId);

    // 로그인 ID 중복 여부
    boolean existsByLoginId(String loginId);

    // 조직 정보까지 함께 로드
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.organization WHERE u.id = :id")
    Optional<User> findByIdFetchOrganization(@Param("id") Long id);

    // 조직 미가입자 검색
    @Query("""
           SELECT u FROM User u
            WHERE u.organization IS NULL
              AND (u.name LIKE %:keyword% OR u.loginId LIKE %:keyword%)
           """)
    List<User> searchInvitableUsers(@Param("keyword") String keyword);

    // (선택) DM 후보 조회 시 본인 제외하고 가져올 때 유용
    List<User> findByIdNot(Long meId);
}