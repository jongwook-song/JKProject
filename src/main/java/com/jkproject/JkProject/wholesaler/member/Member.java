package com.jkproject.JkProject.wholesaler.member;

import com.jkproject.JkProject.wholesaler.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loginId;
    private String password;
    private String name;
    private String address;
    @Enumerated(EnumType.STRING)
    private Role role;
}