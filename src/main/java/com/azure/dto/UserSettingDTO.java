package com.azure.dto;
import lombok.Data;

@Data
public class UserSettingDTO {
    private Long userId;
    private Boolean receiveEmail;
    private Boolean receivePush;
    private String defaultView;
}
