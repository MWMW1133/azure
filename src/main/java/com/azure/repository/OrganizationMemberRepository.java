package com.azure.repository;

import com.azure.model.OrganizationMember;
import com.azure.model.OrganizationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, OrganizationMemberId> {

    @Query("""
        SELECT om
        FROM OrganizationMember om
        JOIN FETCH om.user u
        WHERE om.organization.id = :organizationId
        """)
    List<OrganizationMember> findByOrganizationIdFetchUser(@Param("organizationId") Long organizationId);

    List<OrganizationMember> findByOrganizationId(Long organizationId);
    List<OrganizationMember> findByUserId(Long userId);

    // 유저의 조직-역할 정보 조회
    @Query("""
            SELECT om 
            FROM OrganizationMember om
            JOIN FETCH om.organization o
            WHERE om.user.id = :userId
            """)
    List<OrganizationMember> findByUserIdFetchOrganization(@Param("userId") Long userId);

}
