package com.library.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Tên Exchange (Trạm trung chuyển tin nhắn)
    public static final String RESERVATION_EXCHANGE = "library.reservation.exchange";

    // Tên Queue (Hàng đợi chứa tin nhắn)
    public static final String RESERVATION_FULFILLED_QUEUE = "library.reservation.fulfilled.queue";
    public static final String BORROWING_CREATED_QUEUE = "library.borrowing.created.queue";
    public static final String REQUEST_APPROVED_QUEUE = "library.request.approved.queue";
    public static final String REQUEST_REJECTED_QUEUE = "library.request.rejected.queue";

    // Routing Key (Mã định tuyến / Địa chỉ thư)
    public static final String RESERVATION_FULFILLED_ROUTING_KEY = "reservation.fulfilled";
    public static final String BORROWING_CREATED_ROUTING_KEY = "borrowing.created";
    public static final String REQUEST_APPROVED_ROUTING_KEY = "request.approved";
    public static final String REQUEST_REJECTED_ROUTING_KEY = "request.rejected";

    // Tạo Queue với durable = true (Tin nhắn không bị mất khi sập server) 4
    @Bean
    public Queue reservationFulfilledQueue() {
        return new Queue(RESERVATION_FULFILLED_QUEUE, true);
    }

    @Bean
    public Queue borrowingCreatedQueue() {
        return new Queue(BORROWING_CREATED_QUEUE, true);
    }

    @Bean
    public Queue requestApprovedQueue() {
        return new Queue(REQUEST_APPROVED_QUEUE, true);
    }

    @Bean
    public Queue requestRejectedQueue() {
        return new Queue(REQUEST_REJECTED_QUEUE, true);
    }

    // Tạo Direct Exchange
    @Bean
    public DirectExchange reservationExchange() {
        return new DirectExchange(RESERVATION_EXCHANGE);
    }

    // Nối Queue vào Exchange thông qua Routing Key
    @Bean
    public Binding bindingReservationFulfilled(Queue reservationFulfilledQueue, DirectExchange reservationExchange) {
        return BindingBuilder.bind(reservationFulfilledQueue)
                .to(reservationExchange)
                .with(RESERVATION_FULFILLED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingBorrowingCreated(Queue borrowingCreatedQueue, DirectExchange reservationExchange) {
        return BindingBuilder.bind(borrowingCreatedQueue)
                .to(reservationExchange)
                .with(BORROWING_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingRequestApproved(Queue requestApprovedQueue, DirectExchange reservationExchange) {
        return BindingBuilder.bind(requestApprovedQueue)
                .to(reservationExchange)
                .with(REQUEST_APPROVED_ROUTING_KEY);
    }

    @Bean
    public Binding bindingRequestRejected(Queue requestRejectedQueue, DirectExchange reservationExchange) {
        return BindingBuilder.bind(requestRejectedQueue)
                .to(reservationExchange)
                .with(REQUEST_REJECTED_ROUTING_KEY);
    }

    // Chuyển đổi dữ liệu Java Object sang định dạng JSON để truyền qua mạng
    @Bean
    public MessageConverter jsonMessageConverter(tools.jackson.databind.json.JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}
