package com.library.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CreateBookRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 40, message = "tiêu đề tối đa 40 chữ, vui lòng kiểm tra lại")
    String title;

    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(max = 40, message = "Tên tác giả tối đa 40 ký tự, vui lòng kiểm tra lại")
    String author;

    @NotBlank(message = "ISBN không được để trống")
    @Size(max = 13, message = "ISBN tối đa 13 ký tự, vui lòng kiểm tra lại")
    String isbn;

    @Size(max = 50, message = "Nhà xuất bản tối đa 50 ký tự")
    String publisher;

    @NotBlank(message = "Danh mục không được để trống")
    @Size(max = 40, message = "Danh mục tối đa 40 chữ, vui lòng kiểm tra lại")
    String category;

    String description;

    @NotNull(message = "Năm xuất bản không được để trống")
    @Positive(message = "Năm xuất bản phải là số dương")
    @Max(value = 2026, message = "Năm xuất bản tối đa 2026, vui lòng kiểm tra lại")
    Integer publicationYear;

    String imageUrl;

    @DecimalMin(value = "0.0", message = "Số tiền phạt theo ngày không được là số âm")
    BigDecimal dailyFineAmount;

    @DecimalMin(value = "0.0", message = "Phí đền bù không được là số âm")
    BigDecimal replacementFee;

}
