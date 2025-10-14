<%--<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>--%>
<%--<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>--%>
<%--<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>--%>

<%--  <!-- 원본 폰트 링크 (변경 금지) -->--%>
<%--  <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.css">--%>
<%--    <aside class="sidebar" role="navigation" aria-label="Sidebar">--%>
<%--      <div class="sidebar-inner">--%>
<%--        <!-- 고정 3개 -->--%>
<%--        <nav class="nav-fixed">--%>
<%--          <a class="nav-item ${activePage eq 'home' ? 'active' : ''}"--%>
<%--            href="${pageContext.request.contextPath}/home">--%>
<%--            <span class="ic">--%>
<%--              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">--%>
<%--                <path d="M3 10.5L12 4l9 6.5"></path>--%>
<%--                <path d="M6 10v8.5a1.5 1.5 0 0 0 1.5 1.5H16.5A1.5 1.5 0 0 0 18 18.5V10"></path>--%>
<%--              </svg>--%>
<%--            </span>--%>
<%--            <span>홈</span>--%>
<%--          </a>--%>

<%--          <a class="nav-item ${activePage eq 'tasks' ? 'active' : ''}"--%>
<%--            href="${pageContext.request.contextPath}/tasks/my">--%>
<%--            <span class="ic">--%>
<%--              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">--%>
<%--                <path d="M3 7h18"></path>--%>
<%--                <rect x="4" y="7" width="16" height="12" rx="2"></rect>--%>
<%--                <path d="M9 3h6a2 2 0 0 1 2 2v2H7V5a2 2 0 0 1 2-2z"></path>--%>
<%--                <path d="M9 12h6"></path>--%>
<%--              </svg>--%>
<%--            </span>--%>
<%--            <span>내 작업</span>--%>
<%--          </a>--%>

<%--          <a class="nav-item ${activePage eq 'calendar' ? 'active' : ''}"--%>
<%--            href="${pageContext.request.contextPath}/calendar">--%>
<%--            <span class="ic">--%>
<%--            <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">--%>
<%--              <rect x="3" y="5" width="18" height="16" rx="3"></rect>--%>
<%--              <path d="M8 3v4M16 3v4M3 11h18"></path>--%>
<%--              <circle cx="16" cy="16" r="1.4"></circle>--%>
<%--            </svg>--%>
<%--          </span>--%>
<%--          <span>내 캘린더</span>--%>
<%--          </a>--%>
<%--        </nav>--%>

<%--          <div class="label">워크 스페이스</div>--%>

<%--        <div class="group">--%>
<%--          <div class="group-title">--%>
<%--            <span class="ic">--%>
<%--              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">--%>
<%--                <path d="M3 7h6l2 2h10v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7z"></path>--%>
<%--              </svg>--%>
<%--            </span>--%>
<%--            <span>프로젝트</span>--%>
<%--          </div>--%>

<%--            <div class="proj-list">--%>
<%--                <c:forEach var="proj" items="${projects}">--%>
<%--                    <c:set var="projKey" value="project${proj.id}"/>--%>
<%--                    <a class="proj-row ${activePage eq projKey ? 'active' : ''}"--%>
<%--                    href="${pageContext.request.contextPath}/projects/${proj.id}/tasks">--%>
<%--                        <span class="ic elbow">--%>
<%--                        <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">--%>
<%--                            <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>--%>
<%--                        </svg>--%>
<%--                        </span>--%>
<%--                        <span>${proj.name}</span>--%>
<%--                    </a>--%>
<%--                </c:forEach>--%>
<%--            </div>--%>

<%--          <a class="proj-row proj-plan ${activePage eq 'plan' ? 'active' : ''}"--%>
<%--             href="${pageContext.request.contextPath}/project-plan">--%>
<%--            <span class="ic">--%>
<%--               <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">--%>
<%--                 <path d="M12 20h9"></path>--%>
<%--                 <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L8 18l-4 1 1-4 11.5-11.5z"></path>--%>
<%--               </svg>--%>
<%--            </span>--%>
<%--            <span>프로젝트 계획</span>--%>
<%--          </a>--%>
<%--        </div>--%>


<%--          <!-- 회의실 섹션(독립) : 구분선 + 회의실 -->--%>
<%--          <div class="room-section">--%>
<%--              <hr class="divider divider-room" />--%>
<%--              <a id="nav-room"--%>
<%--                 class="proj-row room ${activePage eq 'meeting' ? 'active' : ''}"--%>
<%--                 href="${pageContext.request.contextPath}/meeting"--%>
<%--                 data-route="meeting">--%>
<%--                <span class="ic">--%>
<%--                <!-- 헤드셋 아이콘 (elbow 제거) -->--%>
<%--                <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">--%>
<%--                    <path d="M4 12a8 8 0 0 1 16 0" />--%>
<%--                    <rect x="3" y="12" width="4" height="7" rx="2" />--%>
<%--                    <rect x="17" y="12" width="4" height="7" rx="2" />--%>
<%--                    <path d="M7 19c2 2 8 2 10 0" />--%>
<%--                </svg>--%>
<%--                </span>--%>
<%--              <span>회의실</span>--%>
<%--              </a>--%>
<%--          </div>--%>

<%--        <div class="sidebar-footer">--%>
<%--          <div class="user-card">--%>
<%--              <!-- 아바타: 이름 첫 글자 or 이미지 -->--%>
<%--              <c:choose>--%>
<%--                  <c:when test="${not empty user.avatarUrl}">--%>
<%--                      <img src="${pageContext.request.contextPath}${user.avatarUrl}"--%>
<%--                           alt="Profile"--%>
<%--                           class="avatar-img rounded-circle"--%>
<%--                           width="32" height="32">--%>
<%--                  </c:when>--%>
<%--                  <c:otherwise>--%>
<%--                      <div class="avatar">--%>
<%--                              ${fn:substring(user.name, 0, 1)}--%>
<%--                      </div>--%>
<%--                  </c:otherwise>--%>
<%--              </c:choose>--%>
<%--            <div>--%>
<%--              <div class="user-name">${user.name}</div>--%>
<%--              <div class="presence"><span class="dot"></span> 접속중</div>--%>
<%--            </div>--%>
<%--            <!-- ▲▲▲ -->--%>
<%--          </div>--%>
<%--        </div>--%>
<%--      </div>--%>
<%--    </aside>--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!-- 원본 폰트 링크 (변경 금지) -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.css">

<aside class="sidebar" role="navigation" aria-label="Sidebar">
    <div class="sidebar-inner">
        <!-- 고정 3개 -->
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

            <div style="background:pink;">DEBUG: activePage = ${activePage}</div>
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

        <div class="group">
            <div class="group-title">
            <span class="ic">
              <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                <path d="M3 7h6l2 2h10v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V7z"></path>
              </svg>
            </span>
                <span>프로젝트</span>
            </div>

            <div class="proj-list">
                <c:forEach var="proj" items="${projects}">
                    <c:set var="projKey" value="project${proj.id}"/>
                    <a class="proj-row ${activePage eq projKey ? 'active' : ''}"
                       href="${pageContext.request.contextPath}/projects/${proj.id}/tasks">
                        <span class="ic elbow">
                        <svg width="22" height="22" viewBox="0 0 24 24" class="stroke-1">
                            <path d="M6 6v8a4 4 0 0 0 4 4h8"></path>
                        </svg>
                        </span>
                        <span>${proj.name}</span>
                    </a>
                </c:forEach>
            </div>

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

        <div class="sidebar-footer">
            <div class="user-card">
                <!-- ▼▼▼ [백엔드 연결 지점] GET /api/me → {name} 받아서 아래 두 텍스트만 교체 -->
                <!-- 아바타: 이름 첫 글자 or 이미지 -->
                <c:choose>
                    <c:when test="${not empty user.avatarUrl}">
                        <img src="${pageContext.request.contextPath}${user.avatarUrl}"
                             alt="Profile"
                             class="avatar-img rounded-circle"
                             width="32" height="32">
                    </c:when>
                    <c:otherwise>
                        <div class="avatar">
                                ${fn:substring(user.name, 0, 1)}
                        </div>
                    </c:otherwise>
                </c:choose>
                <div>
                    <div class="user-name">${user.name}</div>
                    <div class="presence"><span class="dot"></span> 접속중</div>
                </div>
                <!-- ▲▲▲ -->
            </div>
        </div>
    </div>
</aside>