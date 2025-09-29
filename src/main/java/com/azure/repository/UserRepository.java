package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.user.User;
import com.azure.model.user.User.WorkStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByOrganizationIdAndWorkStatus(Long organizationId, WorkStatus workStatus);
    Optional<User> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}
