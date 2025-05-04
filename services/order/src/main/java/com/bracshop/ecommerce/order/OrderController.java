package com.bracshop.ecommerce.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<Integer> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
    return ResponseEntity.ok(this.orderService.createOrder(orderRequest));
  }

  @GetMapping
  public ResponseEntity<List<OrderResponse>> findAll() {
    return ResponseEntity.ok(this.orderService.findAllOrders());
  }

  @GetMapping("/{order-id}")
  public ResponseEntity<OrderResponse> findById(@PathVariable("order-id") Integer orderId) {
    return ResponseEntity.ok(this.orderService.findById(orderId));
  }
}