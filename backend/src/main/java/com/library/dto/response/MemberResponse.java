package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MemberResponse {
    Long id;
    String name;
    String username;
    String memberCode;
    String email;
    String phone;
    String address;
    Boolean isActive;
    String avatar;
    Integer age;

}
