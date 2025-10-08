<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<aside class="sidebar" role="navigation" aria-label="Sidebar">
  <div class="sidebar-inner">

    <!-- 상단 고정 메뉴 -->
    <nav class="nav-fixed">
      <a class="nav-item ${activePage eq 'home' ? 'active' : ''}"
         href="${pageContext.request.contextPath}/home">
        <span class="ic">
          <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
            <path d="M3 10.5L12 4l9 6.5"></path>
            <path d="M6 10v8.5a1.5 1.5 0 0 0 1.5 1.5H16.5A1.5 1.5 0 0 0 18 18.5V10"></path>
          </svg>
        </span>
        <span>홈</span>
      </a>

      <a class="nav-item ${activePage eq 'tasks' ? 'active' : ''}"
         href="${pageContext.request.contextPath}/tasks/my">
        <span class="ic">
          <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
            <path d="M3 7h18"></path>
            <rect x="4" y="7" width="16" height="12" rx="2"></rect>
            <path d="M9 3h6a2 2 0 0 1 2 2v2H7V5a2 2 0 0 1 2-2z"></path>
            <path d="M9 12h6"></path>
          </svg>
        </span>
        <span>내 작업</span>
      </a>

      <a class="nav-item ${activePage eq 'calendar' ? 'active' : ''}"
         href="${pageContext.request.contextPath}/calendar">
        <span class="ic">
          <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
            <rect x="3" y="5" width="18" height="16" rx="3"></rect>
            <path d="M8 3v4M16 3v4M3 11h18"></path>
            <circle cx="16" cy="16" r="1.4"></circle>
          </svg>
        </span>
        <span>내 캘린더</span>
      </a>
    </nav>

    <div class="label">워크 스페이스</div>

    <!-- 프로젝트 그룹 -->
    <div class="group">
      <div class="group-title">
        <span class="ic">
          <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
            <path d="M3 7h6l2 2h10v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7z"></path>
          </svg>
        </span>
        <span>프로젝트</span>
      </div>

      <!-- ▼ 목록 -->
      <div class="proj-list">
        <a class="proj-row ${activePage eq 'project1' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/projects/1/tasks">
          <span class="ic elbow">
            <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
              <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
            </svg>
          </span>
          <span>프로젝트 1</span>
        </a>

        <a class="proj-row ${activePage eq 'project2' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/projects/2/tasks">
          <span class="ic elbow">
            <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
              <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
            </svg>
          </span>
          <span>프로젝트 2</span>
        </a>

        <a class="proj-row ${activePage eq 'project3' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/projects/3/tasks">
          <span class="ic elbow">
            <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
              <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
            </svg>
          </span>
          <span>프로젝트 3</span>
        </a>

        <!-- 프로젝트 계획 -->
        <a class="proj-row proj-plan ${activePage eq 'plan' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/project-plan">
          <span class="ic">
            <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
              <path d="M12 20h9"></path>
              <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4 11.5-11.5z"></path>
            </svg>
          </span>
          <span>프로젝트 계획</span>
        </a>
      </div>
    </div>

    <!-- 회의실 섹션(독립) : 구분선 + 회의실 -->
<div class="room-section">
  <hr class="divider divider-room" />
  <a id="nav-room"
     class="proj-row room ${activePage eq 'meeting' ? 'active' : ''}"
     href="${pageContext.request.contextPath}/meeting"
     data-route="meeting">
    <span class="ic">
      <!-- 헤드셋 아이콘 (elbow 제거) -->
      <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
        <path d="M4 12a8 8 0 0 1 16 0" />
        <rect x="3" y="12" width="4" height="7" rx="2" />
        <rect x="17" y="12" width="4" height="7" rx="2" />
        <path d="M7 19c2 2 8 2 10 0" />
      </svg>
    </span>
    <span>회의실</span>
  </a>
</div>

    <!-- 하단 사용자 카드 -->
    <div class="sidebar-footer">
      <div class="user-card">
        <!-- ▼ GET /api/me 로 받아 교체 -->
        <div class="avatar">이</div>
        <div>
          <div class="user-name">이재환</div>
          <div class="presence"><span class="dot"></span> 접속중</div>
        </div>
        <!-- ▲ -->
      </div>
    </div>

  </div>
</aside>
