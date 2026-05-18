package com.jkproject.JkProject.wholesaler.payment;

import com.jkproject.JkProject.wholesaler.order.Order;
import com.jkproject.JkProject.wholesaler.order.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public void processPayment(Long orderId, String method) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalPrice());
        payment.setPaymentMethod(method);
        payment.setStatus("COMPLETED");

        paymentRepository.save(payment);
        order.setStatus("PAID");
    }
}