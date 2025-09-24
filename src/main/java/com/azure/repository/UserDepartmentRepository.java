package com.azure.repository;

import com.azure.model.UserDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, com.azure.model.UserDepartmentId> {}
