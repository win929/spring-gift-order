package gift.api.member.service;

import gift.api.member.domain.Member;
import gift.api.member.domain.MemberRole;
import gift.api.member.dto.MemberRequestDto;
import gift.api.member.dto.TokenResponseDto;
import gift.api.member.repository.MemberRepository;
import gift.exception.auth.LoginFailedException;
import gift.exception.conflict.EmailDuplicateException;
import gift.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public MemberService(MemberRepository memberRepository, JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public TokenResponseDto registerMember(MemberRequestDto memberRequestDto) {
        memberRepository.findByEmail(memberRequestDto.email()).ifPresent(member -> {
            throw new EmailDuplicateException(memberRequestDto.email());
        });

        String encodedPassword = BCrypt.hashpw(memberRequestDto.password(), BCrypt.gensalt());

        Member member = new Member(
                memberRequestDto.email(),
                encodedPassword,
                MemberRole.USER
        );

        memberRepository.save(member);

        String token = jwtUtil.createToken(member.getEmail(), member.getRole());

        return new TokenResponseDto(token);
    }

    public TokenResponseDto loginMember(MemberRequestDto memberRequestDto) {
        Member member = memberRepository.findByEmail(memberRequestDto.email())
                .orElseThrow(LoginFailedException::new);

        if (!BCrypt.checkpw(memberRequestDto.password(), member.getPassword())) {
            throw new LoginFailedException();
        }

        String token = jwtUtil.createToken(member.getEmail(), member.getRole());

        return new TokenResponseDto(token);
    }
}
