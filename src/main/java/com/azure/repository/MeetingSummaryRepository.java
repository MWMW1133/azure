package com.azure.repository;

import com.azure.model.MeetingSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingSummaryRepository extends JpaRepository<MeetingSummary, Long> {}
