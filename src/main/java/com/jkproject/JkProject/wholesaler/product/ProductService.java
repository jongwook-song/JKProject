package com.jkproject.JkProject.wholesaler.product;

import com.jkproject.JkProject.wholesaler.member.Member;
import com.jkproject.JkProject.wholesaler.member.MemberRepository;
import com.jkproject.JkProject.wholesaler.product.dto.ProductRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public Long registerProduct(ProductRequestDto dto) {
        Member wholesaler = memberRepository.findById(dto.getWholesalerId())
            .orElseThrow(() -> new RuntimeException("도매업체를 찾을 수 없습니다."));

        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setWholesaler(wholesaler);

        return productRepository.save(product).getId();
    }
}