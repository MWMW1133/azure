package com.azure.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/projectTest")
    public String projectTest() {
        // /WEB-INF/views/projectTest.jsp 로 포워딩
        return "projectTest";
    }
}
