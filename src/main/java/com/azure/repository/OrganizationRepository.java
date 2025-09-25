package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> { }
