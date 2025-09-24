package com.azure.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

public class UserSettingDTO {
    private Long userId;
    private Boolean receiveEmail;
    private Boolean receivePush;
    private String defaultView;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getReceiveEmail() {
        return receiveEmail;
    }

    public void setReceiveEmail(Boolean receiveEmail) {
        this.receiveEmail = receiveEmail;
    }

    public Boolean getReceivePush() {
        return receivePush;
    }

    public void setReceivePush(Boolean receivePush) {
        this.receivePush = receivePush;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }
}
