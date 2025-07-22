package gift.config;

import gift.api.member.domain.Member;
import gift.api.member.domain.MemberRole;
import gift.api.member.repository.MemberRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;

    public DataInitializer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@admin";
        String adminPassword = "admin123";

        if (memberRepository.findByEmail(adminEmail).isEmpty()) {
            Member admin = new Member(
                    adminEmail,
                    BCrypt.hashpw(adminPassword, BCrypt.gensalt()),
                    MemberRole.ADMIN
            );
            memberRepository.save(admin);
        }
    }
}