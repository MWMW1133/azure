package com.azure.controller.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MeetingPageController {

    // application.properties에서 Agora App ID와 S3 URL을 주입받습니다.
    @Value("${agora.app-id}")
    private String agoraAppId;

    @Value("${aws.s3.public-base-url}")
    private String s3PublicBaseUrl;

    @GetMapping("/meeting") // 페이지 경로는 /meeting
    public String meetingPage(
            @RequestParam(required = false) Long projectId,
            Model model) {

        // JSP에 필요한 변수들을 전달합니다.
        model.addAttribute("AGORA_APP_ID", agoraAppId);
        model.addAttribute("AWS_S3_PUBLIC_BASE_URL", s3PublicBaseUrl);
        model.addAttribute("projectId", projectId);

        // 보여줄 JSP 경로 설정
        model.addAttribute("body", "/WEB-INF/views/meeting.jsp");
        model.addAttribute("activePage", "meeting"); // 사이드바 활성화를 위해 추가

        // 최종적으로 mainbar.jsp를 렌더링
        return "mainbar";
    }
}
