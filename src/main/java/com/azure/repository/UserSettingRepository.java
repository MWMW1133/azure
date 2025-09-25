package com.azure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azure.model.user.UserSetting;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> { }
