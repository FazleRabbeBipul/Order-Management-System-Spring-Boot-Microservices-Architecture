package com.bracshop.ecommerce.order;

import com.bracshop.ecommerce.customer.CustomerClient;
import com.bracshop.ecommerce.exception.BusinessException;
import com.bracshop.ecommerce.kafka.OrderConfirmation;
import com.bracshop.ecommerce.kafka.OrderProducer;
import com.bracshop.ecommerce.orderline.OrderLineRequest;
import com.bracshop.ecommerce.orderline.OrderLineService;
import com.bracshop.ecommerce.payment.PaymentClient;
import com.bracshop.ecommerce.payment.PaymentRequest;
import com.bracshop.ecommerce.product.ProductClient;
import com.bracshop.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;

    @Transactional
    public Integer createOrder(OrderRequest orderRequest) {
        //Finding customer from customer-service
        var customer = customerClient.findCustomerById(orderRequest.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));

        //Purchase product from product-service
        var purchasedProducts = productClient.purchaseProducts(orderRequest.products());

        //Save order
        var order = orderRepository.save(orderMapper.toOrder(orderRequest));

        //Save OrderLine
        for (PurchaseRequest purchaseRequest : orderRequest.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        //Proceed payment to Payment-service
        var paymentRequest = new PaymentRequest(
                orderRequest.amount(),
                orderRequest.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        //Sending message to kafka
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        orderRequest.reference(),
                        orderRequest.amount(),
                        orderRequest.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }

    public List<OrderResponse> findAllOrders() {
        return this.orderRepository.findAll()
                .stream()
                .map(this.orderMapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer id) {
        return this.orderRepository.findById(id)
                .map(this.orderMapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", id)));
    }
}