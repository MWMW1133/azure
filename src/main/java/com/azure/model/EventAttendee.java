package com.azure.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="event_attendees",
  uniqueConstraints=@UniqueConstraint(name="uq_event_att", columnNames={"project_calendar_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventAttendee extends BaseTimeEntity {
  @EmbeddedId
  private EventAttendeeId id;

  @MapsId("projectCalendarId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="project_calendar_id")
  private ProjectCalendarEvent event;

  @MapsId("userId")
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id")
  private User user;

  @Column(length=20)
  private String rsvp; // going/maybe/no
}
