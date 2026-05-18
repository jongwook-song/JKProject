package com.jkproject.JkProject.wholesaler.member;

import com.jkproject.JkProject.wholesaler.member.dto.MemberJoinDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Long join(MemberJoinDto dto) {
        if(memberRepository.findByLoginId(dto.getLoginId()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        Member member = new Member();
        member.setLoginId(dto.getLoginId());
        member.setPassword(dto.getPassword());
        member.setName(dto.getName());
        member.setAddress(dto.getAddress());
        member.setRole(dto.getRole());
        return memberRepository.save(member).getId();
    }
}