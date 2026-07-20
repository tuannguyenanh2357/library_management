package com.library.service.interfaces;

import com.library.dto.response.BorrowingResponse;
import com.library.dto.request.BorrowingCreationRequest;

import java.util.List;

public interface BorrowingService {
    BorrowingResponse borrowBook(BorrowingCreationRequest request);
    BorrowingResponse returnBook(Long borrowingId);
    BorrowingResponse getById(Long id);
    List<BorrowingResponse> getAll();
    List<BorrowingResponse> getByMemberId(Long memberId);
    List<BorrowingResponse> getOverdueBorrowings();
    List<BorrowingResponse> getByCopyId(Long copyId);
    void deleteBorrowing(Long borrowingId);
    
}
