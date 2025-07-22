package gift.api.member.controller;

import gift.api.member.dto.MemberRequestDto;
import gift.api.member.dto.TokenResponseDto;
import gift.api.member.service.MemberService;
import gift.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    public MemberController(MemberService memberService, JwtUtil jwtUtil) {
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponseDto> registerMember(
            @Valid @RequestBody MemberRequestDto memberRequestDto,
            HttpServletResponse response
    ) {
        TokenResponseDto tokenResponse = memberService.registerMember(memberRequestDto);

        jwtUtil.addJwtToCookie(tokenResponse.token(), response);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginMember(
            @Valid @RequestBody MemberRequestDto memberRequestDto,
            HttpServletResponse response
    ) {
        TokenResponseDto tokenResponse = memberService.loginMember(memberRequestDto);

        jwtUtil.addJwtToCookie(tokenResponse.token(), response);

        return ResponseEntity.ok(tokenResponse);
    }
}
