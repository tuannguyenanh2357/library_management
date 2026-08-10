package com.library.service.impl;

import com.library.dto.request.ReservationCreationRequest;
import com.library.dto.response.ReservationResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.Member;
import com.library.entity.Reservation;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.mapper.ReservationMapper;
import com.library.repository.BookRepository;
import com.library.repository.BookCopyRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.MemberRepository;
import com.library.repository.ReservationRepository;
import com.library.service.interfaces.ReservationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.MemberNotFoundException;
import com.library.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.library.config.RabbitMQConfig;
import com.library.dto.event.ReservationFulfilledEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationServiceImpl implements ReservationService {

    ReservationRepository reservationRepository;
    MemberRepository memberRepository;
    BookRepository bookRepository;
    BookCopyRepository bookCopyRepository;
    BorrowingRepository borrowingRepository;
    ReservationMapper mapper;
    RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse createReservation(ReservationCreationRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy độc giả"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BOOK_NOT_FOUND));

        // Kiểm tra xem độc giả đã có đặt chỗ đang hoạt động cho sách này chưa
        boolean exists = reservationRepository.existsByMemberIdAndBookIdAndStatusIn(
                member.getId(), book.getId(), Arrays.asList(ReservationStatus.PENDING, ReservationStatus.FULFILLED));

        if (exists) {
            throw new AppException(ErrorCode.RESERVATION_FAILED, "Bạn đã đặt trước cuốn sách này rồi.");
        }

        // Kiểm tra xem có bản sao nào khả dụng không. Nếu có, người dùng
        // nên mượn trực tiếp hoặc tạo Yêu cầu mượn (BorrowingRequest).
        // Thực tế, Đặt chỗ (Reservation) dành cho trường hợp sách đã hết.
        long availableCount = bookCopyRepository.countByBook_IdAndStatus(book.getId(), BookCopyStatus.AVAILABLE);
        if (availableCount > 0) {
            throw new AppException(ErrorCode.RESERVATION_FAILED,
                    "Sách này vẫn còn bản sao sẵn sàng. Vui lòng mượn trực tiếp thay vì đặt trước.");
        }

        Reservation reservation = Reservation.builder()
                .member(member)
                .book(book)
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponseWithExpectedDate(saved);
    }

    @Override
    public List<ReservationResponse> getMyReservations(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy thành viên"));

        return reservationRepository.findByMemberIdOrderByRequestDateDesc(member.getId())
                .stream()
                .map(this::mapToResponseWithExpectedDate)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getPendingReservationsForBook(Long bookId) {
        return reservationRepository.findByBookIdAndStatusOrderByRequestDateAsc(bookId, ReservationStatus.PENDING)
                .stream()
                .map(this::mapToResponseWithExpectedDate)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .sorted((a, b) -> b.getRequestDate().compareTo(a.getRequestDate()))
                .map(this::mapToResponseWithExpectedDate)
                .collect(Collectors.toList());
    }

    private ReservationResponse mapToResponseWithExpectedDate(Reservation reservation) {
        ReservationResponse response = mapper.toResponse(reservation);
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            response.setExpectedAvailableDate(
                    borrowingRepository.findEarliestDueDateByBookId(reservation.getBook().getId()));
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND,
                        "Không tìm thấy thông tin đặt trước"));

        // Chỉ người sở hữu hoặc admin mới được hủy. Chúng ta đơn giản hóa bằng cách chỉ
        // kiểm tra
        // người sở hữu nếu đó không phải là API của admin.
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberNotFoundException("Không tìm thấy người dùng"));

        if (!reservation.getMember().getId().equals(member.getId())
                && member.getRole() != com.library.entity.enums.MemberRole.ADMIN
                && member.getRole() != com.library.entity.enums.MemberRole.LIBRARIAN) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền hủy đặt chỗ này");
        }

        if (reservation.getStatus() == ReservationStatus.FULFILLED) {
            // Sách đã được giữ, nếu hủy thì sách phải về AVAILABLE và chuyển cho người tiếp
            // theo (nếu có)
            BookCopy copy = reservation.getFulfilledCopy();
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);

            if (copy != null) {
                // Sách này không còn bị giữ bởi người này nữa
                fulfillNextReservationIfAny(reservation.getBook().getId(), copy);
            }
        } else if (reservation.getStatus() == ReservationStatus.PENDING) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        } else {
            throw new AppException(ErrorCode.RESERVATION_FAILED, "Không thể hủy ở trạng thái hiện tại");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fulfillNextReservationIfAny(Long bookId, BookCopy returnedCopy) {
        // 1. Tìm người đặt chỗ SỚM NHẤT đang ở trạng thái PENDING
        Optional<Reservation> nextReservation = reservationRepository
                .findFirstByBookIdAndStatusOrderByRequestDateAsc(bookId, ReservationStatus.PENDING);

        if (nextReservation.isPresent()) {
            Reservation res = nextReservation.get();
            res.setStatus(ReservationStatus.FULFILLED); // đổi sang trạng thái đã có sách
            res.setFulfilledCopy(returnedCopy);
            res.setFulfilledDate(LocalDateTime.now());
            res.setExpiryDate(LocalDateTime.now().plusHours(48));

            returnedCopy.setStatus(BookCopyStatus.RESERVED); // giữ sách lại

            reservationRepository.save(res);
            bookCopyRepository.save(returnedCopy);

            // Gửi event qua RabbitMQ
            ReservationFulfilledEvent event = ReservationFulfilledEvent.builder()
                    .memberEmail(res.getMember().getEmail())
                    .memberName(res.getMember().getName())
                    .bookTitle(res.getBook().getTitle())
                    .expiryDate(res.getExpiryDate())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVATION_EXCHANGE,
                    RabbitMQConfig.RESERVATION_FULFILLED_ROUTING_KEY, event);

        } else {
            returnedCopy.setStatus(BookCopyStatus.AVAILABLE);
            bookCopyRepository.save(returnedCopy);
        }
    }

    @Override
    public boolean hasPendingReservations(Long bookId) {
        return !reservationRepository
                .findByBookIdAndStatusOrderByRequestDateAsc(bookId, ReservationStatus.PENDING)
                .isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeReservationByCopyId(Long copyId) {
        reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.FULFILLED && r.getFulfilledCopy() != null
                        && r.getFulfilledCopy().getId().equals(copyId))
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(ReservationStatus.COMPLETED);
                    reservationRepository.save(r);
                });
    }
}
