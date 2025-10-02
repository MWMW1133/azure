package com.azure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.azure.dto.UserRequestDTO;
import com.azure.service.UserService;

@Controller
public class UserController {

    private final UserService userService;

    // 생성자 주입
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입 데이터 처리
    @PostMapping("/signup-process")
    public String signUp(UserRequestDTO signUpDto) {
        boolean isSuccess = userService.signUp(signUpDto);

        if (isSuccess) {
            return "redirect:/login"; // 회원가입 성공 시 로그인 페이지로 이동
        } else {
            // 에러
            return "redirect:/signup?error";
        }
    }

    // 로그인 데이터 처리
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        boolean isSuccess = userService.login(username, password);

        if (isSuccess) {
            return "redirect:/"; // 로그인 성공 시 홈
        } else {
            // 로그인 실패
            return "redirect:/login?error";
        }
    }

}