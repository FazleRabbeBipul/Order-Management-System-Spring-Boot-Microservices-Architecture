package com.bracshop.ecommerce.payment;

import com.bracshop.ecommerce.customer.CustomerResponse;
import com.bracshop.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}