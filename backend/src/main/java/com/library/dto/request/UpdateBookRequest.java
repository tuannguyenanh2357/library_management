package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.Max;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateBookRequest {
    @NotBlank(message = "Tiêu đề bắt buộc phải có")
    @Size(max = 40, message = "tiêu đề tối đa 40 chữ, vui lòng kiểm tra lại")
    String title;

    @NotBlank(message = "Tác giả bắt buộc phải có")
    @Size(max = 40, message = "Tên tác giả tối đa 40 chữ, vui lòng kiểm tra lại")
    String author;

    String publisher;

    @NotBlank(message = "Danh mục bắt buộc phải có")
    @Size(max = 40, message = "Danh mục tối đa 40 chữ, vui lòng kiểm tra lại")
    String category;

    String description;

    @NotNull(message = "Năm xuất bản bắt buộc phải có")
    @Positive(message = "Năm xuất bản phải là số dương")
    @Max(value = 2026, message = "Năm xuất bản tối đa 2026, vui lòng kiểm tra lại")
    Integer publicationYear;

    String imageUrl;

    BigDecimal dailyFineAmount;

    BigDecimal replacementFee;

}
