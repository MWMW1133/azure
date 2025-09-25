package com.azure.repository;

import com.azure.model.MeetingTranscriptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscriptEntity, Long> {
}
