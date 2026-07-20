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

    public static final String RESERVATION_EXCHANGE = "library.reservation.exchange";
    public static final String RESERVATION_FULFILLED_QUEUE = "library.reservation.fulfilled.queue";
    public static final String RESERVATION_FULFILLED_ROUTING_KEY = "reservation.fulfilled";

    @Bean
    public Queue reservationFulfilledQueue() {
        return new Queue(RESERVATION_FULFILLED_QUEUE, true); // durable = true
    }

    @Bean
    public DirectExchange reservationExchange() {
        return new DirectExchange(RESERVATION_EXCHANGE);
    }

    @Bean
    public Binding bindingReservationFulfilled(Queue reservationFulfilledQueue, DirectExchange reservationExchange) {
        return BindingBuilder.bind(reservationFulfilledQueue).to(reservationExchange)
                .with(RESERVATION_FULFILLED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(tools.jackson.databind.json.JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }
}
