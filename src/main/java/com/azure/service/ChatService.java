package com.azure.service;

import com.azure.model.chat.ChatChannel;
import com.azure.model.chat.Message;
import com.azure.model.enums.ChannelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 채팅 도메인 서비스 (채널/멤버/메시지/읽음표시)
 *
 * <h2>역할</h2>
 * <ul>
 *   <li>컨트롤러에서 레포지토리 직접 접근을 막고 비즈니스 규칙(멤버십, 참조 유효성, 읽음 위치 처리)을 캡슐화</li>
 *   <li>트랜잭션 경계 제공: 쓰기 메서드는 기본 트랜잭션, 조회는 readOnly 최적화</li>
 * </ul>
 *
 * <h2>일반 규칙</h2>
 * <ul>
 *   <li>채널/메시지 접근은 <b>채널 멤버</b>만 가능(권한 체크는 서비스에서 1차 방어 권장)</li>
 *   <li>파일/답글(스레드) 참조 시 <b>존재 여부 + 동일 채널</b> 검증</li>
 * </ul>
 */
public interface ChatService {

    /** 채널 생성(프로젝트/그룹/DM 등). 필수값 검증(타입/이름), 필요 시 프로젝트 ID 연결. */
    ChatChannel createChannel(ChannelType type, Long projectId, String name, Long createdBy);

    /** 채널 멤버 추가/삭제. 중복 추가는 무시(멱등) 또는 예외 중 택1(구현에 따름). */
    void addMember(Long channelId, Long userId);
    void removeMember(Long channelId, Long userId);

    /** 메시지 전송(파일/답글 옵션 포함). 내용 공백/멤버십/참조 유효성 체크. */
    Message postMessage(Long channelId, Long authorId, String body, Long fileId, Long replyToId);

    /** 메시지 목록(페이징). 보통 채널 멤버만 열람 가능. */
    Page<Message> listMessages(Long channelId, Pageable pageable);

    /** 읽음 위치 업데이트(해당 채널의 마지막 읽은 메시지 ID 저장). */
    void markRead(Long channelId, Long userId, Long lastReadMessageId);

    /** DM 채널을 찾거나 생성해서 ID 반환 */
    Long getOrCreateDmChannel(long me, long peer);
}
