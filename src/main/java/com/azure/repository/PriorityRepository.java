package com.azure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.task.Priority;

public interface PriorityRepository extends JpaRepository<Priority, Integer> {
    // name으로 우선순위 조회
    Optional<Priority> findByName(String name);
 }
