package com.azure.repository;

import com.azure.model.MeetingTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingTranscriptRepository extends JpaRepository<MeetingTranscript, Long> {}
