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
@Transactional
public class ReservationServiceImpl implements ReservationService {

    ReservationRepository reservationRepository;
    MemberRepository memberRepository;
    BookRepository bookRepository;
    BookCopyRepository bookCopyRepository;
    BorrowingRepository borrowingRepository;
    ReservationMapper mapper;
    RabbitTemplate rabbitTemplate;

    @Override
    public ReservationResponse createReservation(ReservationCreationRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy độc giả"));
        
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        // Check if member already has an active reservation for this book
        boolean exists = reservationRepository.existsByMemberIdAndBookIdAndStatusIn(
                member.getId(), book.getId(), Arrays.asList(ReservationStatus.PENDING, ReservationStatus.FULFILLED));
        
        if (exists) {
            throw new RuntimeException("Bạn đã đặt trước cuốn sách này rồi.");
        }

        // Check if there are available copies. If there is an available copy, user should just borrow it directly or create BorrowingRequest.
        // Actually, Reservation is for when it's out of stock.
        long availableCount = bookCopyRepository.countByBook_IdAndStatus(book.getId(), BookCopyStatus.AVAILABLE);
        if (availableCount > 0) {
            throw new RuntimeException("Sách này vẫn còn bản sao sẵn sàng. Vui lòng mượn trực tiếp thay vì đặt trước.");
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên"));
        
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
            response.setExpectedAvailableDate(borrowingRepository.findEarliestDueDateByBookId(reservation.getBook().getId()));
        }
        return response;
    }

    @Override
    public void cancelReservation(Long reservationId, String username) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đặt trước"));

        // Only the owner or an admin should cancel. We'll simplify to just checking the owner if it's not an admin API.
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!reservation.getMember().getId().equals(member.getId()) && member.getRole() != com.library.entity.enums.MemberRole.ADMIN && member.getRole() != com.library.entity.enums.MemberRole.LIBRARIAN) {
            throw new RuntimeException("Bạn không có quyền hủy đặt chỗ này");
        }

        if (reservation.getStatus() == ReservationStatus.FULFILLED) {
            // Sách đã được giữ, nếu hủy thì sách phải về AVAILABLE và chuyển cho người tiếp theo (nếu có)
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
            throw new RuntimeException("Không thể hủy ở trạng thái hiện tại");
        }
    }

    @Override
    public void fulfillNextReservationIfAny(Long bookId, BookCopy returnedCopy) {
        Optional<Reservation> nextReservation = reservationRepository.findFirstByBookIdAndStatusOrderByRequestDateAsc(bookId, ReservationStatus.PENDING);
        
        if (nextReservation.isPresent()) {
            Reservation res = nextReservation.get();
            res.setStatus(ReservationStatus.FULFILLED);
            res.setFulfilledCopy(returnedCopy);
            res.setFulfilledDate(LocalDateTime.now());
            res.setExpiryDate(LocalDateTime.now().plusHours(48)); // 48 giờ để đến lấy sách
            
            returnedCopy.setStatus(BookCopyStatus.RESERVED);
            
            reservationRepository.save(res);
            bookCopyRepository.save(returnedCopy);
            
            // Gửi event qua RabbitMQ thay vì gửi email trực tiếp đồng bộ
            ReservationFulfilledEvent event = ReservationFulfilledEvent.builder()
                    .memberEmail(res.getMember().getEmail())
                    .memberName(res.getMember().getName())
                    .bookTitle(res.getBook().getTitle())
                    .expiryDate(res.getExpiryDate())
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVATION_EXCHANGE, RabbitMQConfig.RESERVATION_FULFILLED_ROUTING_KEY, event);
            
        } else {
            returnedCopy.setStatus(BookCopyStatus.AVAILABLE);
            bookCopyRepository.save(returnedCopy);
        }
    }

    @Override
    public void completeReservationByCopyId(Long copyId) {
        reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.FULFILLED && r.getFulfilledCopy() != null && r.getFulfilledCopy().getId().equals(copyId))
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(ReservationStatus.COMPLETED);
                    reservationRepository.save(r);
                });
    }
}
