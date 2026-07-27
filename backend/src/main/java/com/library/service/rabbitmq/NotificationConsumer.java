package com.library.service.rabbitmq;

import com.library.config.RabbitMQConfig;
import com.library.dto.event.BorrowingCreatedEvent;
import com.library.dto.event.ReservationFulfilledEvent;
import com.library.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_FULFILLED_QUEUE)
    public void handleReservationFulfilled(ReservationFulfilledEvent event) {
        log.info("=========================================================");
        log.info("[RABBITMQ CONSUMER] Nhận được yêu cầu gửi thông báo sách về!");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedExpiry = event.getExpiryDate() != null ? event.getExpiryDate().format(formatter) : "N/A";

        String subject = "THÔNG BÁO: Sách đặt trước đã có sẵn tại thư viện";
        String text = String.format("Kính gửi %s,\n\n" +
                        "Cuốn sách '%s' bạn đặt trước đã có sẵn tại thư viện.\n" +
                        "Vui lòng đến nhận sách trước %s tại địa điểm: 123 Nguyễn Khang, Hà Nội. Sau thời gian này, sách sẽ được chuyển cho người khác.\n\n" +
                        "Trân trọng,\nBan Quản lý Thư viện",
                event.getMemberName(), event.getBookTitle(), formattedExpiry);

        emailService.sendEmail(event.getMemberEmail(), subject, text);

        log.info("Đã gửi email thành công tới: {}", event.getMemberEmail());
        log.info("=========================================================");
    }

    @RabbitListener(queues = RabbitMQConfig.BORROWING_CREATED_QUEUE)
    public void handleBorrowingCreated(BorrowingCreatedEvent event) {
        log.info("=========================================================");
        log.info("[RABBITMQ CONSUMER] Nhận được yêu cầu gửi thông báo tạo đơn mượn sách!");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedBorrowDate = event.getBorrowDate() != null ? event.getBorrowDate().format(formatter) : "N/A";
        String formattedDueDate = event.getDueDate() != null ? event.getDueDate().format(formatter) : "N/A";

        String subject = "THÔNG BÁO: Xác nhận mượn sách thành công";
        String text = String.format("Kính gửi %s,\n\n" +
                        "Bạn đã mượn thành công cuốn sách '%s' tại Thư viện.\n" +
                        "- Ngày mượn: %s\n" +
                        "- Hạn trả sách: %s\n\n" +
                        "Vui lòng trả sách đúng hạn để tránh phát sinh phí phạt quá hạn.\n\n" +
                        "Trân trọng,\nBan Quản lý Thư viện",
                event.getMemberName(), event.getBookTitle(), formattedBorrowDate, formattedDueDate);

        emailService.sendEmail(event.getMemberEmail(), subject, text);

        log.info("Đã gửi email xác nhận mượn sách tới độc giả: {}", event.getMemberEmail());
        log.info("=========================================================");
    }
}

