package gift.api.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import gift.api.member.domain.Member;
import gift.api.member.domain.MemberRole;
import gift.api.member.dto.MemberRequestDto;
import gift.api.member.dto.TokenResponseDto;
import gift.api.member.repository.MemberRepository;
import gift.exception.auth.LoginFailedException;
import gift.exception.conflict.EmailDuplicateException;
import gift.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtil jwtUtil;

    private MockedStatic<BCrypt> bCrypt;

    @BeforeEach
    void setUp() {
        // BCrypt의 static 메서드 mocking
        bCrypt = mockStatic(BCrypt.class);
    }

    @AfterEach
    void tearDown() {
        // static mock 해제
        bCrypt.close();
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerMember_success() {
        // given
        MemberRequestDto requestDto = new MemberRequestDto("new@test.com", "password");
        given(memberRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        bCrypt.when(() -> BCrypt.hashpw("password", BCrypt.gensalt()))
                .thenReturn("hashed_password");
        given(jwtUtil.createToken("new@test.com", MemberRole.USER)).willReturn("Bearer test_token");

        // when
        TokenResponseDto response = memberService.registerMember(requestDto);

        // then
        assertThat(response.token()).isEqualTo("Bearer test_token");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerMember_fail_duplicateEmail() {
        // given
        MemberRequestDto requestDto = new MemberRequestDto("exist@test.com", "password");
        Member existingMember = new Member("exist@test.com", "pw", MemberRole.USER);
        given(memberRepository.findByEmail("exist@test.com")).willReturn(
                Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> memberService.registerMember(requestDto))
                .isInstanceOf(EmailDuplicateException.class);
    }

    @Test
    @DisplayName("로그인 성공")
    void loginMember_success() {
        // given
        MemberRequestDto requestDto = new MemberRequestDto("user@test.com", "password");
        Member member = new Member("user@test.com", "hashed_password", MemberRole.USER);
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        bCrypt.when(() -> BCrypt.checkpw("password", "hashed_password")).thenReturn(true);
        given(jwtUtil.createToken("user@test.com", MemberRole.USER)).willReturn(
                "Bearer test_token");

        // when
        TokenResponseDto response = memberService.loginMember(requestDto);

        // then
        assertThat(response.token()).isEqualTo("Bearer test_token");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void loginMember_fail_emailNotFound() {
        // given
        MemberRequestDto requestDto = new MemberRequestDto("no@test.com", "password");
        given(memberRepository.findByEmail("no@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.loginMember(requestDto))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginMember_fail_passwordMismatch() {
        // given
        MemberRequestDto requestDto = new MemberRequestDto("user@test.com", "wrong_password");
        Member member = new Member("user@test.com", "hashed_password", MemberRole.USER);
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        bCrypt.when(() -> BCrypt.checkpw("wrong_password", "hashed_password")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> memberService.loginMember(requestDto))
                .isInstanceOf(LoginFailedException.class);
    }
}