package com.jkproject.JkProject.wholesaler.payment;

import com.jkproject.JkProject.wholesaler.order.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int amount;
    private String paymentMethod;
    private String status;
}
