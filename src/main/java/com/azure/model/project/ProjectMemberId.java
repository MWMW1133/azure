package com.azure.model;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;
@Data @Embeddable
public class ProjectMemberId implements Serializable {
    private Long projectId;
    private Long userId;
}
