package com.azure.repository;

import com.azure.model.MeetingSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingSummaryRepository extends JpaRepository<MeetingSummaryEntity, Long> {
}
