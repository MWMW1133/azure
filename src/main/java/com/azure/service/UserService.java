package com.azure.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.azure.dto.UserRequestDTO;

@Service
public class UserService {

    // 임시 DB
    private final Map<String, String> userDatabase = new HashMap<>();

    /**
     * 회원가입
     * @param signUpDto 회원가입 폼에서 받은 데이터
     * @return 성공 시 true, 아이디 중복 시 false
     */
    public boolean signUp(UserRequestDTO userDTO) {
        // 아이디 중복 확인
        if (userDatabase.containsKey(userDTO.getUserId())) {
            System.out.println("이미 존재하는 아이디입니다");
            return false; 
        }

        // 새 사용자 등록
        userDatabase.put(userDTO.getUserId(), userDTO.getUserPwd());
        System.out.println("회원가입 성공");
        return true;
    }

    /**
     * 로그인 
     * @param userId 사용자 아이디
     * @param password 사용자 비밀번호
     * @return 로그인 성공 시 true, 실패 시 false
     */
    public boolean login(String userId, String password) {
        // 아이디 존재 여부 확인
        if (!userDatabase.containsKey(userId)) {
            System.out.println("존재하지 않는 아이디입니다: " + userId);
            return false;
        }

        // 비밀번호 일치 여부 확인
        String storedPassword = userDatabase.get(userId);
        if (!Objects.equals(storedPassword, password)) {
            System.out.println("비밀번호가 일치하지 않습니다.");
            return false;
        }

        System.out.println("로그인 성공: " + userId);
        return true;
    }
}