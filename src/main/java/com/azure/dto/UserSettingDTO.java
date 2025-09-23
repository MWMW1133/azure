package com.azure.dto;


public class UserSettingDTO {
    private int userId;
    private boolean receiveEmail;
    private boolean receivePush;
    private String defaultView;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean getReceiveEmail() {
        return receiveEmail;
    }

    public void setReceiveEmail(boolean receiveEmail) {
        this.receiveEmail = receiveEmail;
    }

    public boolean getReceivePush() {
        return receivePush;
    }

    public void setReceivePush(boolean receivePush) {
        this.receivePush = receivePush;
    }

    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }
}
