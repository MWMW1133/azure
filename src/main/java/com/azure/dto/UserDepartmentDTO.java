package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class UserDepartmentDTO {
    private Long userId;
    private Long departmentId;
    private String roleInDept;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getRoleInDept() {
        return roleInDept;
    }

    public void setRoleInDept(String roleInDept) {
        this.roleInDept = roleInDept;
    }
}
