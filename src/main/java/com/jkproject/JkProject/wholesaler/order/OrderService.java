package com.jkproject.JkProject.wholesaler.order;

import com.jkproject.JkProject.wholesaler.cart.Cart;
import com.jkproject.JkProject.wholesaler.cart.CartRepository;
import com.jkproject.JkProject.wholesaler.cartItem.CartItem;
import com.jkproject.JkProject.wholesaler.cartItem.CartItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public Long createOrderFromCart(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
            .orElseThrow(() -> new RuntimeException("장바구니가 비어있습니다."));

        Order order = new Order();
        order.setBuyer(cart.getMember());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("WAITING_PAYMENT");

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.createOrderItem(
                cartItem.getProduct(),
                cartItem.getProduct().getPrice(),
                cartItem.getCount()
            );

            order.addOrderItem(orderItem);
            cartItem.getProduct().removeStock(cartItem.getCount());
        }

        orderRepository.save(order);
        cartItemRepository.deleteAllByCartId(cart.getId());
        return order.getId();
    }
}