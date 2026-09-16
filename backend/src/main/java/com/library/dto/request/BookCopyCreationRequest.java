package com.library.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BookCopyCreationRequest {
    @NotNull(message = "ID đầu sách không được để trống")
    Long bookId;

    @Size(max = 40, message = "Mã barcode tối đa 40 ký tự, vui lòng kiểm tra lại")
    String barCode;

    @Min(value = 1, message = "Số lượng bản sao mỗi lần thêm phải từ 1 đến 50")
    @Max(value = 50, message = "Số lượng bản sao mỗi lần thêm phải từ 1 đến 50")
    Integer quantity;
}


