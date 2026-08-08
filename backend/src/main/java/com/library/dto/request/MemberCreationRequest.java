package com.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MemberCreationRequest {
    @NotBlank(message = "Name is required")
    String name;

    @NotBlank(message = "Username is required")
    String username;

    @NotBlank(message = "Password is required")
    @Size(message = "Password must be at least 8 characters long", min = 8)
    String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Phone is required")
    String phone;

    @NotBlank(message = "Address is required")
    String address;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be greater than 0")
    Integer age;

}
