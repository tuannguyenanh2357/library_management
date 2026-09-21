package com.library.dto.response;

import com.library.entity.enums.MemberRole;

public interface MemberSummaryProjection {
    Long getId();
    String getUsername();
    String getPhone();
    String getEmail();
    String getMemberCode();
    String getAddress();
    Boolean getIsActive();
    Integer getAge();
    MemberRole getRole();
}
