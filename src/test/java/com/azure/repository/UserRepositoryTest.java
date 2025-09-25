package com.azure.repository;

import com.azure.model.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Rollback(false)   // ✅ 테스트가 끝나도 롤백하지 않음 → 실제 DB에 INSERT 반영됨
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // ✅ 실제 MySQL 사용
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testInsertUser() {
        // given
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPasswordHash("pwHash");
        user.setName("테스트유저");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        // when
        UserEntity saved = userRepository.save(user);

        // then
        assertThat(saved.getId()).isNotNull();
        System.out.println("✅ 저장된 유저 ID = " + saved.getId());
    }
}
