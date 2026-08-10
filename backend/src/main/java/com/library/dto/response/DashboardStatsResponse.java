package com.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalBooks;
    private long totalBookCopies;
    private long totalMembers;
    private long activeBorrowings;
    private long overdueBorrowings;
}
