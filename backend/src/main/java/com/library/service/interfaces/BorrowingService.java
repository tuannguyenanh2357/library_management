package com.library.service.interfaces;

import com.library.dto.response.BorrowingResponse;
import com.library.dto.response.OverdueBookProjection;
import com.library.dto.request.BorrowingCreationRequest;

import java.util.List;

public interface BorrowingService {
    BorrowingResponse borrowBook(BorrowingCreationRequest request);
    BorrowingResponse returnBook(Long borrowingId);
    BorrowingResponse getById(Long id);
    List<BorrowingResponse> getAll();
    List<BorrowingResponse> getByMemberId(Long memberId);
    List<BorrowingResponse> getOverdueBorrowings();
    List<OverdueBookProjection> getOverdueBooksFromSP();
    List<BorrowingResponse> getByCopyId(Long copyId);
    void deleteBorrowing(Long borrowingId);
    BorrowingResponse renewBorrowing(Long borrowingId, String username);
    BorrowingResponse reportLost(Long borrowingId);
    BorrowingResponse reportDamaged(Long borrowingId);
}
