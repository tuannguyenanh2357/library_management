package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CreateBookRequest {
    @NotBlank(message = "Title is required")
    String title;

    @NotBlank(message = "Author is required")
    String author;

    @NotBlank(message = "ISBN is required")
    String isbn;

    String publisher;

    @NotBlank(message = "Category is required")
    String category;

    String description;

    @NotNull(message = "Publication year is required")
    Integer publicationYear;

    String imageUrl;

}
