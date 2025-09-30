package com.azure.service;

import com.azure.model.file.FileObject;

/**
 * 실시간 STT가 끝난 뒤(녹음 종료 버튼) "최종 텍스트"를
 * 1) 파일(.txt/.md)로 저장하고 file_objects에 기록
 * 2) meeting_transcripts에도 본문을 저장
 * 까지 한 번에 처리하는 서비스.
 */
public interface TranscriptService {
    /**
     * 최종 전사 텍스트를 파일로 저장하고 연동 테이블에 기록한다.
     * @param meetingId  회의 ID(meetings.id)
     * @param uploaderId 저장자(현재 로그인 사용자) ID
     * @param lang       언어코드(ko-KR 등)
     * @param content    최종 전사 텍스트
     * @return 생성된 FileObject (file_objects 레코드)
     */
    FileObject saveFinalTranscript(Long meetingId, Long uploaderId, String lang, String content);
}
