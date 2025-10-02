package com.azure.service;

import com.azure.model.calendar.PersonalCalendar;
import com.azure.model.calendar.ProjectCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 캘린더 도메인 서비스 (프로젝트 일정 + 개인 일정)
 *
 * <h2>이 인터페이스가 하는 일</h2>
 * <ul>
 *   <li>컨트롤러가 레포지토리(DAO)에 직접 접근하지 않도록 중간 계층 제공</li>
 *   <li>시간 검증(start &lt; end), 완료 상태 변경, 리마인더 연결 등 비즈니스 규칙 캡슐화</li>
 *   <li>읽기/쓰기 트랜잭션 경계를 서비스 단에서 일관되게 관리</li>
 * </ul>
 *
 * <h2>스키마 관련 주의 (현재 테이블 정의)</h2>
 * <ul>
 *   <li><b>personal_calendars.is_done</b>: TINYINT(1) NOT NULL DEFAULT 0 → JPA Boolean과 매핑됨.
 *       <br/>⚠️ DB DEFAULT는 INSERT 시 해당 컬럼을 완전히 생략해야 적용됨.
 *       JPA가 null을 넣으면 DEFAULT가 <i>적용되지 않음</i> → <b>서비스에서 false를 명시</b>하는 게 안전.</li>
 *   <li><b>personal_calendars.all_day</b>: TINYINT(1) DEFAULT 0 → 전달값이 null이면 false로 강제 설정 권장.</li>
 *   <li><b>created_at / updated_at</b>: DB가 자동 관리. 엔티티에서는 보통
 *       <code>insertable=false, updatable=false</code>로 둬서 애플리케이션이 건드리지 않도록 한다.</li>
 *   <li><b>created_by</b>: users(id) FK / ON DELETE NO ACTION → <i>참조 중인 사용자 삭제 불가</i>.
 *       서비스/관리자 기능 설계 시 주의.</li>
 * </ul>
 *
 * <h2>시간/타임존</h2>
 * <ul>
 *   <li>입력은 <code>LocalDateTime</code> 기준(서버 타임존). 클라이언트 타임존 변환은 컨트롤러/프론트에서.</li>
 *   <li><code>startAt &lt; endAt</code> 검증은 구현체에서 수행. 위반 시 <code>BadRequestException</code> 권장.</li>
 * </ul>
 */
public interface CalendarService {

    /**
     * 프로젝트 일정 생성
     *
     * @param projectId  프로젝트 ID (FK, 필수)
     * @param title      제목(필수)
     * @param description 설명(선택)
     * @param startAt    시작 시각(필수)
     * @param endAt      종료 시각(필수, startAt 이후)
     * @param allDay     종일 여부(선택; null이면 false로 간주해 저장하는 것을 권장)
     * @param rrule      반복 규칙 문자열(선택, iCal 규격 등)
     * @param location   장소(선택)
     * @param createdBy  생성자 사용자 ID(선택)
     * @return 저장된 ProjectCalendar 엔티티
     */
    ProjectCalendar scheduleProjectEvent(Long projectId, String title, String description,
                                         LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                         String rrule, String location, Long createdBy);

    /**
     * 개인 일정 생성
     *
     * <p>개인 일정에는 완료 여부(<b>is_done</b>)가 있으며, <b>기본값 false</b>로 저장한다.
     * DB DEFAULT(0)에만 의존하지 말고 서비스에서 <b>false를 명시</b>해 주는 게 안전하다.</p>
     *
     * @param creatorId  생성자 사용자 ID(선택, FK: created_by)
     * @param title      제목(필수)
     * @param description 설명(선택)
     * @param startAt    시작 시각(필수)
     * @param endAt      종료 시각(필수, startAt 이후)
     * @param allDay     종일 여부(선택; null이면 false로 간주)
     * @param rrule      반복 규칙 문자열(선택)
     * @param location   장소(선택)
     * @return 저장된 PersonalCalendar 엔티티
     */
    PersonalCalendar schedulePersonalEvent(Long creatorId, String title, String description,
                                           LocalDateTime startAt, LocalDateTime endAt, Boolean allDay,
                                           String rrule, String location);

    /**
     * 프로젝트 일정 목록(페이징)
     * @param projectId 프로젝트 ID (필수)
     * @param pageable  페이지/정렬 정보
     */
    Page<ProjectCalendar> listProjectEvents(Long projectId, Pageable pageable);

    /**
     * 개인 일정 목록(페이징)
     *
     * <p>실서비스에서는 레포지토리에
     * <code>findByCreatedBy_Id(userId, Pageable)</code> 같은 메서드를 추가해
     * <b>사용자 기준으로 페이징</b>하는 것을 권장.
     * 임시 버전에서는 <code>findAll(pageable)</code>로 반환할 수 있다.</p>
     *
     * @param userId   사용자 ID (필수; created_by)
     * @param pageable 페이지/정렬 정보
     */
    Page<PersonalCalendar> listPersonalEvents(Long userId, Pageable pageable);

    /** 프로젝트 일정 삭제 (존재하지 않으면 NotFoundException) */
    void deleteProjectEvent(Long eventId);

    /** 개인 일정 삭제 (존재하지 않으면 NotFoundException) */
    void deletePersonalEvent(Long eventId);

    /**
     * 프로젝트 일정 리마인더 추가
     * @param eventId       프로젝트 일정 ID (필수)
     * @param userId        대상 사용자 ID (필수)
     * @param minutesBefore 몇 분 전 알림 (0 이상)
     * @param method        알림 방식(예: EMAIL, PUSH)
     */
    void addReminderForProjectEvent(Long eventId, Long userId, int minutesBefore, String method);

    /**
     * 개인 일정 리마인더 추가
     * @param eventId       개인 일정 ID (필수)
     * @param userId        대상 사용자 ID (필수)
     * @param minutesBefore 몇 분 전 알림 (0 이상)
     * @param method        알림 방식
     */
    void addReminderForPersonalEvent(Long eventId, Long userId, int minutesBefore, String method);

    /**
     * 개인 일정 완료/미완료 설정
     * @param eventId 개인 일정 ID
     * @param done    true=완료, false=미완료
     * @return 갱신된 PersonalCalendar
     */
    PersonalCalendar setPersonalEventDone(Long eventId, boolean done);
}
