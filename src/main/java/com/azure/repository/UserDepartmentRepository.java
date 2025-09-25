package com.azure.repository;

import com.azure.model.UserDepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDepartmentRepository extends JpaRepository<UserDepartmentEntity, Long> {
}
