package com.azure.service;
import org.springframework.stereotype.Service;

import com.azure.dto.ProjectProposalDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectProposalService {

    // 모든 더미 데이터를 생성하는 private 메소드
    private List<ProjectProposalDto> createAllDummyProposals() {
        List<ProjectProposalDto> dummyList = new ArrayList<>();
        // ID, 제안자ID, 회사ID, 프로젝트ID, 제목, 제안자명, 설명, 상태, 생성일, 시작일, 종료일
        dummyList.add(new ProjectProposalDto(1L, 101L, 1L, 1L, "신규 CRM 구축", "김하나", "images/my-cat.png", "영업팀 효율 증대를 위한 시스템", "new", LocalDate.now().minusDays(2), LocalDate.now().plusDays(10), LocalDate.now().plusDays(100)));
        dummyList.add(new ProjectProposalDto(2L, 102L, 1L, 2L, "푸시 알림 기능 강화", "이두리",  "images/my-cat.png","사용자 리텐션 증대를 위한 기능", "new", LocalDate.now().minusDays(1), LocalDate.now().plusDays(20), LocalDate.now().plusDays(80)));
        dummyList.add(new ProjectProposalDto(3L, 103L, 1L, 3L, "인트라넷 UI/UX 개편", "박세나",  "images/my-cat.png","직원 사용성 개선", "approved", LocalDate.now().minusDays(15), LocalDate.now(), LocalDate.now().plusDays(120)));
        dummyList.add(new ProjectProposalDto(4L, 101L, 1L, 4L, "데이터 분석 플랫폼 도입", "김하나",  "images/my-cat.png","빅데이터 기반 의사결정 지원", "approved", LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), LocalDate.now().plusDays(60)));
        dummyList.add(new ProjectProposalDto(5L, 104L, 1L, 5L, "블록체인 신원 인증 개발", "정네모",  "images/my-cat.png","기술적 난이도 및 비용 문제로 보류", "rejected", LocalDate.now().minusDays(30), null, null));
        return dummyList;
    }

    // '새로운 계획' 목록만 필터링해서 반환
    public List<ProjectProposalDto> getNewProposals() {
        return createAllDummyProposals().stream()
                .filter(p -> "new".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    // '승인된 계획' 목록만 필터링해서 반환
    public List<ProjectProposalDto> getApprovedProposals() {
        return createAllDummyProposals().stream()
                .filter(p -> "approved".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    // '거부된 계획' 목록만 필터링해서 반환
    public List<ProjectProposalDto> getRejectedProposals() {
        return createAllDummyProposals().stream()
                .filter(p -> "rejected".equals(p.getStatus()))
                .collect(Collectors.toList());
    }
}