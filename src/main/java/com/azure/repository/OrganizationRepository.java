package com.azure.repository;

import com.azure.dto.UserRole;
import com.azure.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /** 특정 사용자(userId)가 속한 모든 조직(회사) */
    @Query("""
           select o
           from Organization o
           where o.user.id = :userId
           """)
    List<Organization> findAllByUserId(@Param("userId") Long userId);

    /** 조직의 관리자(리더) 사용자 ID 목록 */
    @Query("""
           select o.user.id
           from Organization o
           where o.id = :orgId
             and o.role in :roles
           """)
    List<Long> findAdminUserIds(@Param("orgId") Long orgId,
                                @Param("roles") List<UserRole> roles);


}
