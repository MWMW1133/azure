package com.azure.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class EventAttendeeId implements Serializable {
  private Long projectCalendarId;
  private Long userId;
}
