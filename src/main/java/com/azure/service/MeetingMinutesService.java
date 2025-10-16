package com.azure.service;

import com.azure.model.meeting.Meeting;
import com.azure.model.meeting.MeetingSummary;

public interface MeetingMinutesService {
    /** 전사→요약→저장 (업서트) */
    MeetingSummary summarizeAndSave(Long meetingId);

    /** 최신 회의록 1건 조회 (없으면 null) */
    MeetingSummary getLatest(Long meetingId);

    /** 회의 존재/권한 체크는 컨트롤러/Advice 쪽 정책에 맞게 별도 처리 가능 */
}
