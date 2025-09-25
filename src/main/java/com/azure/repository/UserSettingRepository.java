package com.azure.repository;

import com.azure.model.UserSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingRepository extends JpaRepository<UserSettingEntity, Long> {
}
