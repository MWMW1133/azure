package com.azure.model.project;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor // ✅ 생성자(Long projectId, Long userId) 자동 생성
@Embeddable
public class ProjectMemberId implements Serializable {

    private Long projectId;
    private Long userId;

    // equals/hashCode는 @Data가 자동 생성하지만,
    // Embeddable ID는 반드시 override하는 걸 권장 (JPA 동작 안정성)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectMemberId that)) return false;
        return Objects.equals(projectId, that.projectId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, userId);
    }
}
