package com.jkproject.JkProject.wholesaler;

import com.jkproject.JkProject.wholesaler.cart.CartService;
import com.jkproject.JkProject.wholesaler.member.MemberService;
import com.jkproject.JkProject.wholesaler.member.dto.MemberJoinDto;
import com.jkproject.JkProject.wholesaler.order.OrderService;
import com.jkproject.JkProject.wholesaler.payment.PaymentService;
import com.jkproject.JkProject.wholesaler.product.ProductService;
import com.jkproject.JkProject.wholesaler.product.dto.ProductRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WholesaleController {

    private final MemberService memberService;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberJoinDto dto) {
        memberService.join(dto);
        return ResponseEntity.ok("회원가입 완료");
    }

    // 상품 등록
    @PostMapping("/products")
    public ResponseEntity<String> addProduct(@RequestBody ProductRequestDto dto) {
        productService.registerProduct(dto);
        return ResponseEntity.ok("상품 등록 완료");
    }

    // 장바구니 담기
    @PostMapping("/cart")
    public ResponseEntity<String> addCart(@RequestParam Long memberId, @RequestParam Long productId, @RequestParam int count) {
        cartService.addCart(memberId, productId, count);
        return ResponseEntity.ok("장바구니 담기 완료");
    }

    // 주문하기 (장바구니 -> 주문)
    @PostMapping("/orders")
    public ResponseEntity<Long> createOrder(@RequestParam Long memberId) {
        Long orderId = orderService.createOrderFromCart(memberId);
        return ResponseEntity.ok(orderId);
    }

    // 결제하기
    @PostMapping("/payments")
    public ResponseEntity<String> pay(@RequestParam Long orderId, @RequestParam String method) {
        paymentService.processPayment(orderId, method);
        return ResponseEntity.ok("결제 완료");
    }
}