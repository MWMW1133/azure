package com.azure.model;

import com.azure.model.enums.OrganizationRole;
import com.azure.model.user.User;
import jakarta.persistence.*;
import lombok.Data;


/* 기존의 회사 테이블은 한 회사당 하나의 관리자로 엔티티관계가 묶여있어,
이를 수정하고자 해당 엔티티를 만듬.
관리자-구성원 규칙을 여기서 관리함
 */
@Data
@Entity
@Table(name = "organization_members")
public class OrganizationMember {
    @EmbeddedId
    private OrganizationMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationRole role; // MANAGER or MEMBER
}
