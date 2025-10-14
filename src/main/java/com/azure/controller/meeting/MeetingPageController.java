package com.azure.controller.meeting;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // ❗️ @RestController가 아닌 @Controller를 사용합니다.
@RequiredArgsConstructor
public class MeetingPageController {

    // application.properties에서 S3 URL을 읽어옴
    @Value("${aws.s3.public-base-url}")
    private String s3PublicBaseUrl;

    @GetMapping("/meeting") // 페이지 경로는 /meeting
    public String meetingPage(
            @RequestParam(required=false) Long projectId,
            Model model) {

        // JSP에 변수 전달
        model.addAttribute("AWS_S3_PUBLIC_BASE_URL", s3PublicBaseUrl);
        model.addAttribute("projectId", projectId);

        // 보여줄 JSP 경로 설정
        model.addAttribute("body", "/WEB-INF/views/meeting.jsp");

        // 최종적으로 mainbar.jsp를 렌더링
        return "mainbar";
    }
}