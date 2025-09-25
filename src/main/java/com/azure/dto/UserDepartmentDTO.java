package com.azure.dto;
import lombok.Data;

@Data
public class UserDepartmentDTO {
    private Long userId;
    private Long departmentId;
    private String roleInDept;
}
