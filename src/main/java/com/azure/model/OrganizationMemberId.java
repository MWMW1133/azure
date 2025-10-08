package com.azure.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OrganizationMemberId implements Serializable {
    // private Long id;     // organization_id
    @Column(name = "organization_id")
    private Long organizationId;
    // private Long user;   // user_id
    @Column(name = "user_id")
    private Long userId;
}
