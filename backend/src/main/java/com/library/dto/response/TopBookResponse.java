package com.library.dto.response;

public record TopBookResponse(
        Long bookId,
        String title,
        String author,
        String category,
        String imageUrl,
        Integer borrowCount) {
}
