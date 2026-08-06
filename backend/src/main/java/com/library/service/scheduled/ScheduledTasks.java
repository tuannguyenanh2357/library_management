package com.library.service.scheduled;

import com.library.entity.BookCopy;
import com.library.entity.Borrowing;
import com.library.entity.BorrowingRequest;
import com.library.entity.Reservation;
import com.library.entity.enums.BorrowingRequestStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.repository.BorrowingRepository;
import com.library.repository.BorrowingRequestRepository;
import com.library.repository.ReservationRepository;
import com.library.service.interfaces.EmailService;
import com.library.service.interfaces.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduledTasks {

    ReservationRepository reservationRepository;
    ReservationService reservationService;
    BorrowingRepository borrowingRepository;
    BorrowingRequestRepository borrowingRequestRepository;
    EmailService emailService;

    @NonFinal
    @Value("${app.library.fine.default-daily-amount}")
    BigDecimal defaultDailyFineAmount;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Hết hạn các reservation đã được giữ chỗ (FULFILLED) quá 48h mà độc giả chưa
    // đến lấy
    // @Scheduled(cron = "0 5 0 * * *")
    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "expireFulfilledReservationsTask", lockAtLeastFor = "1m", lockAtMostFor = "5m")
    @Transactional(rollbackFor = Exception.class)
    public void expireFulfilledReservations() {
        List<Reservation> expired;
        try {
            expired = reservationRepository.findExpiredFulfilledReservations();
        } catch (Exception e) {
            log.error("[ScheduledTasks] Lỗi khi truy vấn reservation hết hạn: {}", e.getMessage());
            return;
        }

        for (Reservation reservation : expired) {
            try {
                BookCopy copy = reservation.getFulfilledCopy();
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                if (copy != null) {
                    reservationService.fulfillNextReservationIfAny(reservation.getBook().getId(), copy);
                }
                log.info("[ScheduledTasks] Đã hủy giữ chỗ quá hạn cho reservation id={}", reservation.getId());
            } catch (Exception e) {
                log.error("[ScheduledTasks] Lỗi khi xử lý reservation id={}: {}", reservation.getId(), e.getMessage());
            }
        }
    }

    // Gửi email nhắc nhở cho các phiếu mượn đang quá hạn chưa trả
    @Scheduled(cron = "0 0 8 * * *")
    // @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "sendOverdueRemindersTask", lockAtLeastFor = "1m", lockAtMostFor = "5m")
    @Transactional(rollbackFor = Exception.class)
    public void sendOverdueReminders() {
        List<Borrowing> overdue;
        try {
            overdue = borrowingRepository.findOverdueBorrowings(LocalDate.now());
        } catch (Exception e) {
            log.error("[ScheduledTasks] Lỗi khi truy vấn phiếu mượn quá hạn: {}", e.getMessage());
            return;
        }

        for (Borrowing borrowing : overdue) {
            try {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDate.now());
                var book = borrowing.getBookCopy().getBook();
                BigDecimal dailyFine = book.getDailyFineAmount() != null
                        ? book.getDailyFineAmount()
                        : defaultDailyFineAmount;

                // Tính tổng tiền phạt hiện tại
                BigDecimal totalFine = dailyFine.multiply(BigDecimal.valueOf(daysOverdue));

                String subject = "THÔNG BÁO: Sách mượn đã quá hạn trả";
                String text = String.format("Kính gửi %s,\n\n" +
                        "Cuốn sách '%s' bạn mượn đã quá hạn trả %d ngày (hạn trả: %s).\n" +
                        "Mức phạt là %s VNĐ/ngày. TỔNG SỐ TIỀN PHẠT TẠM TÍNH ĐẾN HÔM NAY LÀ: %s VNĐ.\n" +
                        "Vui lòng mang sách đến trả sớm để tránh phát sinh thêm phí phạt.\n\n"
                        +
                        "Hoặc có thể thanh toán qua STK:"
                        +
                        "Trân trọng,\nBan Quản lý Thư viện",
                        borrowing.getMember().getName(),
                        book.getTitle(),
                        daysOverdue,
                        borrowing.getDueDate().format(DATE_FORMATTER),
                        dailyFine.toPlainString(),
                        totalFine.toPlainString());

                emailService.sendEmail(borrowing.getMember().getEmail(), subject, text);
            } catch (Exception e) {
                log.error("[ScheduledTasks] Lỗi khi gửi nhắc quá hạn cho borrowing id={}: {}", borrowing.getId(),
                        e.getMessage());
            }
        }
    }

    // Tự động hủy các yêu cầu mượn PENDING quá 3 ngày không được nhân viên xử lý
    // @Scheduled(cron = "0 15 0 * * *")
    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "cancelStalePendingRequestsTask", lockAtLeastFor = "1m", lockAtMostFor = "5m")
    @Transactional(rollbackFor = Exception.class)
    public void cancelStalePendingRequests() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        List<BorrowingRequest> stale;
        try {
            stale = borrowingRequestRepository.findStalePendingRequests(cutoff);
        } catch (Exception e) {
            log.error("[ScheduledTasks] Lỗi khi truy vấn yêu cầu mượn tồn đọng: {}", e.getMessage());
            return;
        }

        for (BorrowingRequest request : stale) {
            try {
                request.setStatus(BorrowingRequestStatus.CANCELLED);
                request.setProcessedDate(LocalDateTime.now());
                request.setNotes("Tự động hủy do quá 3 ngày không được Quản lý xử lý");
                borrowingRequestRepository.save(request);
                log.info("[ScheduledTasks] Đã tự động hủy yêu cầu mượn id={}", request.getId());
            } catch (Exception e) {
                log.error("[ScheduledTasks] Lỗi khi hủy yêu cầu mượn id={}: {}", request.getId(), e.getMessage());
            }
        }
    }
}
