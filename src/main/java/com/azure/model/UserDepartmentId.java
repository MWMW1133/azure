package com.azure.model;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;
@Data @Embeddable
public class UserDepartmentId implements Serializable {
    private Long userId;
    private Long departmentId;
}
