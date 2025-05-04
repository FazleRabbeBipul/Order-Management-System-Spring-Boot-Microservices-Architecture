package com.bracshop.ecommerce.kafka;

import com.bracshop.ecommerce.customer.CustomerResponse;
import com.bracshop.ecommerce.order.PaymentMethod;
import com.bracshop.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products
) {
}