package com.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
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
public class MemberUpdateRequest {
    @NotBlank(message = "Name is required")
    String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "Password is required")
    String password;

    @NotBlank(message = "Phone is required")
    String phone;

    @NotBlank(message = "Address is required")
    String address;

    String avatar;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be greater than 0")
    Integer age;

    String memberCode;
    Boolean isActive;

}
