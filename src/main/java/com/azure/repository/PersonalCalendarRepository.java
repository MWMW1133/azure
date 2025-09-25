package com.azure.repository;

import com.azure.model.PersonalCalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalCalendarRepository extends JpaRepository<PersonalCalendarEntity, Long> {
}
