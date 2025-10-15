package com.azure.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TodoItem {
    // 신규(Topbar)용 필드
    private Long id;
    private String title;
    private String memo;        // description
    private String startAt;     // ISO-8601 "yyyy-MM-ddTHH:mm:ss"
    private String endAt;       // ISO-8601
    private Boolean allDay;
    private Boolean isDone;

    // 홈(JSP) 호환을 위한 캐시 필드 (파싱 불필요)
    private String _timeCache;     // "HH:mm - HH:mm" 또는 "종일"
    private String _statusCache;   // "완료"/"미완료"

    public TodoItem() {}

    // ✅ Topbar/JSON 용
    public TodoItem(Long id, String title, String memo,
                    String startAt, String endAt,
                    Boolean allDay, Boolean isDone) {
        this.id = id;
        this.title = title;
        this.memo = memo;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allDay = allDay;
        this.isDone = isDone;
    }

    // ✅ Home(JSP) 기존 코드 호환용(레거시) 생성자
    public TodoItem(String title, String time, String status, String description) {
        this.title = title;
        this.memo = description;
        this._timeCache = time;
        this._statusCache = status;
        // 상태 문자열을 isDone에 반영(옵션)
        if (status != null) {
            this.isDone = "완료".equals(status);
        }
    }

    // ---------- 표준 게터/세터 ----------
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMemo() { return memo; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
    public Boolean getAllDay() { return allDay; }
    public Boolean getIsDone() { return isDone; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public void setEndAt(String endAt) { this.endAt = endAt; }
    public void setAllDay(Boolean allDay) { this.allDay = allDay; }
    public void setIsDone(Boolean isDone) { this.isDone = isDone; }

    // ---------- 홈(JSP) 호환 게터 ----------
    public String getDescription() { return memo; }

    public String getStatus() {
        if (_statusCache != null) return _statusCache;
        return Boolean.TRUE.equals(isDone) ? "완료" : "미완료";
    }

    public String getTime() {
        if (_timeCache != null) return _timeCache;
        // startAt/endAt 기반으로 HH:mm - HH:mm 구성
        if (Boolean.TRUE.equals(allDay)) return "종일";
        if (startAt == null || endAt == null) return "";
        return toHm(startAt) + " - " + toHm(endAt);
    }

    // ---------- 내부 유틸 ----------
    private static String toHm(String iso) {
        // iso: "yyyy-MM-ddTHH:mm[:ss]"
        try {
            String base = iso.length() >= 16 ? iso.substring(0, 16) : iso;
            LocalDateTime dt = LocalDateTime.parse(base.length()==16 ? base + ":00" : base.replace(' ', 'T'));
            return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ignore) {
            // 파싱 실패하면 뒤 5글자(HH:mm) 추출 시도
            return iso.length() >= 16 ? iso.substring(11, 16) : "";
        }
    }
}
