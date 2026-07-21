package com.library.dto.response;

public interface TopBookProjection {
    Long getBookId();
    String getTitle();
    String getAuthor();
    String getCategory();
    String getImageUrl();
    Long getBorrowCount();
}

