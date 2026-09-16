package com.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class MyProfileUpdateRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng Email không hợp lệ")
    String email;

    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không đúng định dạng VN (VD: 0912345678)")
    String phone;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    String address;
}

