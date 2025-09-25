package com.azure.model;

import java.io.Serializable;
import java.util.Objects;

public class ReminderId implements Serializable {
    private Long projectCalendarId;
    private Integer minutesBefore;

    public ReminderId() {}

    public ReminderId(Long projectCalendarId, Integer minutesBefore) {
        this.projectCalendarId = projectCalendarId;
        this.minutesBefore = minutesBefore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReminderId)) return false;
        ReminderId that = (ReminderId) o;
        return Objects.equals(projectCalendarId, that.projectCalendarId) &&
               Objects.equals(minutesBefore, that.minutesBefore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectCalendarId, minutesBefore);
    }
}
