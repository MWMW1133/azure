package com.azure.repository;

import com.azure.model.PriorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriorityRepository extends JpaRepository<PriorityEntity, Long> {
}
