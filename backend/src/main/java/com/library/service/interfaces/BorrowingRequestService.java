package com.library.service.interfaces;

import com.library.dto.request.BorrowingRequestApprovalRequest;
import com.library.dto.request.BorrowingRequestCreationRequest;
import com.library.dto.response.BorrowingRequestResponse;

import java.util.List;

public interface BorrowingRequestService {
    BorrowingRequestResponse createRequest(BorrowingRequestCreationRequest request);
    List<BorrowingRequestResponse> getPendingRequests();
    List<BorrowingRequestResponse> getHistoryRequests();
    List<BorrowingRequestResponse> getRequestsByMember(Long memberId);
    BorrowingRequestResponse approveRequest(Long requestId, BorrowingRequestApprovalRequest approvalRequest);
    BorrowingRequestResponse rejectRequest(Long requestId, String reason);
}
