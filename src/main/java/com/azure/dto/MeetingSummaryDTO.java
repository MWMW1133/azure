package com.azure.dto;


public class MeetingSummaryDTO {
    private int id;
    private int meetingId;
    private String summaryMd;
    private String actionItems;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    public String getSummaryMd() {
        return summaryMd;
    }

    public void setSummaryMd(String summaryMd) {
        this.summaryMd = summaryMd;
    }

    public String getActionItems() {
        return actionItems;
    }

    public void setActionItems(String actionItems) {
        this.actionItems = actionItems;
    }
}
