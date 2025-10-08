// package com.azure.dto;


// 회사 단위 관리자와 프로젝트 관리자가 따로 있는 건가요?
//public enum UserRole {
//    LEADER,   // 관리자/담당자
//    MEMBER    // 일반 구성원
//}


package com.azure.dto;

import com.azure.model.enums.OrganizationRole;

@Deprecated // 나중에 제거할 예정
public enum UserRole {
    LEADER, MEMBER;

    public OrganizationRole toOrganizationRole() {
        return this == LEADER ? OrganizationRole.MANAGER : OrganizationRole.MEMBER;
    }
}
