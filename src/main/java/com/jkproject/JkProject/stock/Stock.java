package com.jkproject.JkProject.stock;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "stock")
public class Stock {
    @Id
    private String code = "";
    private String name = "";
    private String type = "";
    private String detailType = "";
}