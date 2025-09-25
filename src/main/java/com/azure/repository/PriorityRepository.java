package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.task.Priority;

public interface PriorityRepository extends JpaRepository<Priority, Integer> { }
