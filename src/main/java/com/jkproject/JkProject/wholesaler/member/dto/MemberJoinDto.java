package com.jkproject.JkProject.wholesaler.member.dto;

import com.jkproject.JkProject.wholesaler.Role;
import lombok.Data;

@Data
public class MemberJoinDto {
    private String loginId;
    private String password;
    private String name;
    private String address;
    private Role role;
}