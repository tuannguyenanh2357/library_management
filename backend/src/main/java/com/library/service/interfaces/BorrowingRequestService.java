package com.library.service.interfaces;

import com.library.dto.request.BorrowingRequestApprovalRequest;
import com.library.dto.request.BorrowingRequestCreationRequest;
import com.library.dto.response.BorrowingRequestResponse;

import java.util.List;

public interface BorrowingRequestService {
    BorrowingRequestResponse createRequest(BorrowingRequestCreationRequest request);
    List<BorrowingRequestResponse> getPendingRequests();
    List<BorrowingRequestResponse> getApprovedRequests();
    List<BorrowingRequestResponse> getHistoryRequests();
    List<BorrowingRequestResponse> getRequestsByMember(Long memberId);
    BorrowingRequestResponse approveRequest(Long requestId, BorrowingRequestApprovalRequest approvalRequest);
    BorrowingRequestResponse rejectRequest(Long requestId, String reason);
    BorrowingRequestResponse cancelRequest(Long requestId, String username);
    // Giao sách thực tế: tạo Borrowing, chuyển yêu cầu → COMPLETED
    BorrowingRequestResponse issueBook(Long requestId);
    // Hủy yêu cầu đã duyệt (không đến lấy): trả sách về AVAILABLE, chuyển → EXPIRED
    BorrowingRequestResponse expireRequest(Long requestId);
}
