package com.azure.dto;


public class UserDepartmentDTO {
    private int userId;
    private int departmentId;
    private String roleInDept;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getRoleInDept() {
        return roleInDept;
    }

    public void setRoleInDept(String roleInDept) {
        this.roleInDept = roleInDept;
    }
}
