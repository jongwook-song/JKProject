package com.jkproject.JkProject.wholesaler.product;

import com.jkproject.JkProject.wholesaler.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wholesaler_id")
    private Member wholesaler;

    @Builder
    public Product(String name, int price, int stockQuantity, Member wholesaler) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.wholesaler = wholesaler;
    }

    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) throw new RuntimeException("재고가 부족합니다.");
        this.stockQuantity = restStock;
    }
}
