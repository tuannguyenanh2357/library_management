package com.library.service.impl;

import com.library.dto.request.BorrowingRequestApprovalRequest;
import com.library.dto.request.BorrowingRequestCreationRequest;
import com.library.dto.request.BorrowingCreationRequest;
import com.library.dto.response.BorrowingRequestResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BorrowingRequest;
import com.library.entity.Member;
import com.library.entity.enums.BorrowingRequestStatus;
import com.library.mapper.BorrowingRequestMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRequestRepository;
import com.library.repository.MemberRepository;
import com.library.service.interfaces.BorrowingRequestService;
import com.library.service.interfaces.BorrowingService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class BorrowingRequestServiceImpl implements BorrowingRequestService {

    final BorrowingRequestRepository requestRepository;
    final MemberRepository memberRepository;
    final BookRepository bookRepository;
    final BookCopyRepository bookCopyRepository;
    final BorrowingService borrowingService;
    final BorrowingRequestMapper mapper;

    @Override
    public BorrowingRequestResponse createRequest(BorrowingRequestCreationRequest dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        BorrowingRequest request = BorrowingRequest.builder()
                .member(member)
                .book(book)
                .status(BorrowingRequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .expectedDueDate(dto.getExpectedDueDate())
                .notes(dto.getNotes())
                .build();

        BorrowingRequest saved = requestRepository.save(request);
        long count = bookCopyRepository.countByBook_IdAndStatus(book.getId(), com.library.entity.enums.BookCopyStatus.AVAILABLE);
        return mapper.toResponse(saved, count);
    }

    @Override
    public List<BorrowingRequestResponse> getPendingRequests() {
        return requestRepository.findByStatusWithRelations(BorrowingRequestStatus.PENDING)
                .stream()
                .map(r -> mapper.toResponse(r, bookCopyRepository.countByBook_IdAndStatus(r.getBook().getId(), com.library.entity.enums.BookCopyStatus.AVAILABLE)))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowingRequestResponse> getHistoryRequests() {
        return requestRepository.findHistoryWithRelations()
                .stream()
                .map(r -> mapper.toResponse(r, bookCopyRepository.countByBook_IdAndStatus(r.getBook().getId(), com.library.entity.enums.BookCopyStatus.AVAILABLE)))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowingRequestResponse> getRequestsByMember(Long memberId) {
        return requestRepository.findByMemberIdWithRelations(memberId)
                .stream()
                .map(r -> mapper.toResponse(r, bookCopyRepository.countByBook_IdAndStatus(r.getBook().getId(), com.library.entity.enums.BookCopyStatus.AVAILABLE)))
                .collect(Collectors.toList());
    }

    @Override
    public BorrowingRequestResponse approveRequest(Long requestId, BorrowingRequestApprovalRequest approvalRequest) {
        BorrowingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != BorrowingRequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        // Auto-assign the first available copy
        BookCopy bookCopy = bookCopyRepository.findFirstByBook_IdAndStatus(request.getBook().getId(), com.library.entity.enums.BookCopyStatus.AVAILABLE)
                .orElseThrow(() -> new RuntimeException("Không còn cuốn sách nào khả dụng trong kho"));

        // Use expected due date and notes from request
        BorrowingCreationRequest borrowingDto = new BorrowingCreationRequest(
                request.getMember().getId(),
                bookCopy.getId(),
                request.getExpectedDueDate(),
                request.getNotes()
        );
        borrowingService.borrowBook(borrowingDto);

        request.setStatus(BorrowingRequestStatus.APPROVED);
        request.setProcessedDate(LocalDateTime.now());
        
        long count = bookCopyRepository.countByBook_IdAndStatus(request.getBook().getId(), com.library.entity.enums.BookCopyStatus.AVAILABLE);
        return mapper.toResponse(requestRepository.save(request), count);
    }

    @Override
    public BorrowingRequestResponse rejectRequest(Long requestId, String reason) {
        BorrowingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != BorrowingRequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        request.setStatus(BorrowingRequestStatus.REJECTED);
        request.setProcessedDate(LocalDateTime.now());
        request.setNotes(reason);

        long count = bookCopyRepository.countByBook_IdAndStatus(request.getBook().getId(), com.library.entity.enums.BookCopyStatus.AVAILABLE);
        return mapper.toResponse(requestRepository.save(request), count);
    }
}
