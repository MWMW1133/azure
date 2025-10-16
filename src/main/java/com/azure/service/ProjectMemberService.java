package com.azure.service;

import java.util.List;

public interface ProjectMemberService {
    boolean isMember(Long projectId, Long userId);
    boolean isManager(Long projectId, Long userId);
    List<Long> memberIds(Long projectId);
}
