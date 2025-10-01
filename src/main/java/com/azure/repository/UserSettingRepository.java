package com.azure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.user.UserSetting;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> { 
    // 특정 유저의 설정 조회
    Optional<UserSetting> findByUserId(Long userId);
}
