package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.user.User;
import com.azure.model.user.User.WorkStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 조직 ID로 사용자 목록 조회
    List<User> findByOrganizationIdAndWorkStatus(Long organizationId, WorkStatus workStatus);
    // 로그인 ID로 사용자 조회
    Optional<User> findByLoginId(String loginId);
    // 로그인 ID 존재 여부 확인
    boolean existsByLoginId(String loginId);

    // 사용자 회사 정보 가져오기(조인) (추가)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.organization WHERE u.id = :id")
    Optional<User> findByIdFetchOrganization(@Param("id") Long id);

    // 조직 미가입자 검색
    @Query("SELECT u FROM User u WHERE u.organization IS NULL AND " +
            "(u.name LIKE %:keyword% OR u.loginId LIKE %:keyword%)")
    List<User> searchInvitableUsers(@Param("keyword") String keyword);

}