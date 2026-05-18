package com.jkproject.JkProject.wholesaler.cart;

import com.jkproject.JkProject.wholesaler.cartItem.CartItem;
import com.jkproject.JkProject.wholesaler.cartItem.CartItemRepository;
import com.jkproject.JkProject.wholesaler.member.Member;
import com.jkproject.JkProject.wholesaler.member.MemberRepository;
import com.jkproject.JkProject.wholesaler.product.Product;
import com.jkproject.JkProject.wholesaler.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
 CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public void addCart(Long memberId, Long productId, int count) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        Cart cart = cartRepository.findByMemberId(memberId)
            .orElseGet(() -> cartRepository.save(Cart.createCart(member)));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        Optional<CartItem> foundItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (foundItem.isPresent()) {
            foundItem.get().addCount(count);
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, product, count);
            cartItemRepository.save(cartItem);
        }
    }
}