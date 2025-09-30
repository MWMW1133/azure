package com.azure.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.azure.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> { 
    /** 특정 사용자(userId)가 속한 모든 회사 조회 */
    @Query("SELECT u.organization FROM User u WHERE u.id = :userId")
    Organization findByUserId(@Param("userId") Long userId);

    @Query("""
      select ou.user.id
      from OrganizationUser ou
      where ou.organization.id = :orgId and ou.role in ('ADMIN','OWNER','MANAGER')
    """)
    List<Long> findAdminUserIds(Long orgId);
}
