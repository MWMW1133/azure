package com.azure.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    private String userId;
    private String userPwd;
    private String email;
}