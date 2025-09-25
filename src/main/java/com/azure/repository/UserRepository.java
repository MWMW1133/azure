package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.user.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByOrganizationId(Long organizationId);
}
