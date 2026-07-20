package com.library.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    Long id;
    
    String title;

    String author;

    String isbn;

    String publisher;

    String category;

    String description;

    Integer publicationYear;

    String imageUrl;

    java.math.BigDecimal dailyFineAmount;

    long availableCopiesCount;

}
