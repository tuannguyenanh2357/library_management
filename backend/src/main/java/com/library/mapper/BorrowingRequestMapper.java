package com.library.mapper;

import com.library.dto.response.BorrowingRequestResponse;
import com.library.entity.BorrowingRequest;
import org.springframework.stereotype.Component;

@Component
public class BorrowingRequestMapper {

    public BorrowingRequestResponse toResponse(BorrowingRequest request, long availableCopiesCount) {
        return BorrowingRequestResponse.builder()
                .id(request.getId())
                .memberId(request.getMember().getId())
                .memberName(request.getMember().getName())
                .bookId(request.getBook().getId())
                .bookTitle(request.getBook().getTitle())
                .status(request.getStatus().name())
                .requestDate(request.getRequestDate())
                .expectedDueDate(request.getExpectedDueDate())
                .processedDate(request.getProcessedDate())
                .notes(request.getNotes())
                .availableCopiesCount(availableCopiesCount)
                .assignedBookCopyId(request.getAssignedBookCopyId()) // ID của cuốn sách cụ thể được gán
                .approvedDate(request.getApprovedDate()) // Ngày duyệt yêu cầu
                .build();
    }
}
