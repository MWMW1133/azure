package com.azure.service.impl;

import com.azure.model.Organization;
import com.azure.model.project.Project;
import com.azure.model.meeting.Meeting;
import com.azure.repository.MeetingRepository;
import com.azure.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;

    @Override
    public Meeting startMeeting(Long organizationId, Long projectId) {
        Meeting m = new Meeting();
        if (organizationId != null) { var o = new Organization(); o.setId(organizationId); m.setOrganization(o); }
        if (projectId != null)      { var p = new Project();      p.setId(projectId);      m.setProject(p); }
        m.setStartedAt(LocalDateTime.now());
        return meetingRepository.save(m);
    }

    @Override
    public Meeting endMeeting(Long meetingId) {
        Meeting m = meetingRepository.findById(meetingId).orElseThrow();
        if (m.getEndedAt() == null) m.setEndedAt(LocalDateTime.now());
        return meetingRepository.save(m);
    }
}
