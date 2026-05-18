package com.jkproject.JkProject.wholesaler.product.dto;

import lombok.Data;

@Data
public class ProductRequestDto {
    private Long wholesalerId;
    private String name;
    private int price;
    private int stockQuantity;
}
