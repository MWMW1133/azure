package com.azure.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class OrganizationId implements Serializable {
    private Long id;     // organization_id
    private Long user;   // user_id
}
