package com.library.service.rabbitmq;

import com.library.config.RabbitMQConfig;
import com.library.dto.event.ReservationFulfilledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final com.library.service.interfaces.EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_FULFILLED_QUEUE)
    public void handleReservationFulfilled(ReservationFulfilledEvent event) {
        log.info("=========================================================");
        log.info("[RABBITMQ CONSUMER] Nhận được yêu cầu gửi thông báo sách về!");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedExpiry = event.getExpiryDate() != null ? event.getExpiryDate().format(formatter) : "N/A";
        
        String subject = "THÔNG BÁO: Sách đặt trước đã có sẵn tại thư viện";
        String text = String.format("Kính gửi %s,\n\n" +
                "Cuốn sách '%s' bạn đặt trước đã có sẵn tại thư viện.\n" +
                "Vui lòng đến nhận sách trước %s. Sau thời gian này, sách sẽ được chuyển cho người khác.\n\n" +
                "Trân trọng,\nBan Quản lý Thư viện", 
                event.getMemberName(), event.getBookTitle(), formattedExpiry);

        // Gửi email thực tế
        emailService.sendEmail(event.getMemberEmail(), subject, text);
        
        log.info("Đã gửi email thành công tới: {}", event.getMemberEmail());
        log.info("=========================================================");
    }
}
