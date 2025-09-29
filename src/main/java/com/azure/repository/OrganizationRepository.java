package com.azure.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.azure.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> { 
    /** 특정 사용자(userId)가 속한 모든 회사 조회 */
    @Query("SELECT u.organization FROM User u WHERE u.id = :userId")
    Organization findByUserId(@Param("userId") Long userId);
}
