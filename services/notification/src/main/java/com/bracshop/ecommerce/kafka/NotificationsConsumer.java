package com.bracshop.ecommerce.kafka;

import com.bracshop.ecommerce.email.EmailService;
import com.bracshop.ecommerce.kafka.order.OrderConfirmation;
import com.bracshop.ecommerce.kafka.payment.PaymentConfirmation;
import com.bracshop.ecommerce.notification.Notification;
import com.bracshop.ecommerce.notification.NotificationRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.bracshop.ecommerce.notification.NotificationType.ORDER_CONFIRMATION;
import static com.bracshop.ecommerce.notification.NotificationType.PAYMENT_CONFIRMATION;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationsConsumer {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "payment-topic")
    public void consumePaymentSuccessNotifications(PaymentConfirmation paymentConfirmation) throws MessagingException {
        log.info("Consuming the message from payment-topic Topic:: {}", paymentConfirmation);
        notificationRepository.save(
                Notification.builder()
                        .type(PAYMENT_CONFIRMATION)
                        .notificationDate(LocalDateTime.now())
                        .paymentConfirmation(paymentConfirmation)
                        .build()
        );

        //Sending Email
        var customerName = paymentConfirmation.customerFirstname() + " " + paymentConfirmation.customerLastname();
        emailService.sendPaymentSuccessEmail(paymentConfirmation.customerEmail(), customerName,
                paymentConfirmation.amount(), paymentConfirmation.orderReference());
    }

    @KafkaListener(topics = "order-topic")
    public void consumeOrderConfirmationNotifications(OrderConfirmation orderConfirmation) throws MessagingException {
        log.info("Consuming the message from order-topic Topic:: {}", orderConfirmation);
        notificationRepository.save(
                Notification.builder()
                        .type(ORDER_CONFIRMATION)
                        .notificationDate(LocalDateTime.now())
                        .orderConfirmation(orderConfirmation)
                        .build()
        );

        //Sending Email
        var customerName = orderConfirmation.customer().firstname() + " " + orderConfirmation.customer().lastname();
        emailService.sendOrderConfirmationEmail(orderConfirmation.customer().email(), customerName,
                orderConfirmation.totalAmount(), orderConfirmation.orderReference(), orderConfirmation.products());
    }
}