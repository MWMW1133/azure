package com.azure.model.calendar;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class EventAttendeeId implements Serializable {
    private Long projectId;
    private Long userId;
}
