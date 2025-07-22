package gift.api.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gift.api.member.domain.Member;
import gift.api.member.domain.MemberRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 멤버_조회_성공() {
        String email = "test@example.com";
        Member member = new Member(
                email,
                "password",
                MemberRole.USER
        );
        entityManager.persistAndFlush(member);

        Optional<Member> foundMember = memberRepository.findByEmail(email);

        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo(email);
    }

    @Test
    void 멤버_조회_실패() {
        String email = "nonexistent@example.com";

        Optional<Member> foundMember = memberRepository.findByEmail(email);

        assertThat(foundMember).isNotPresent();
    }

    @Test
    void 멤버_회원가입_실패_중복_이메일() {
        String email = "duplicate@example.com";
        Member member1 = new Member(
                email,
                "password123",
                MemberRole.USER
        );
        entityManager.persistAndFlush(member1);

        Member member2 = new Member(
                email,
                "password456",
                MemberRole.USER
        );

        assertThatThrownBy(() -> memberRepository.saveAndFlush(member2)).
                isInstanceOf(DataIntegrityViolationException.class);
    }
}