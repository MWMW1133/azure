package com.azure.model.project;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class ProjectMemberId implements Serializable {
    private Long projectId;
    private Long userId;
}
