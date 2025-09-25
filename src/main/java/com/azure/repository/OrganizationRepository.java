package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.org.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> { }
