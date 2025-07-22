package gift.api.wish.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gift.api.member.domain.Member;
import gift.api.member.domain.MemberRole;
import gift.api.product.domain.Product;
import gift.api.wish.domain.Wish;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class WishRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WishRepository wishRepository;

    private Member member;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        member = new Member(
                "test@exmaple.com",
                "password",
                MemberRole.USER
        );
        entityManager.persist(member);

        product1 = new Product(
                "Product 1",
                1000L,
                "image1.jpg"
        );
        product2 = new Product(
                "Product 2",
                2000L,
                "image2.jpg"
        );
        entityManager.persist(product1);
        entityManager.persist(product2);

        entityManager.flush();
    }

    @Test
    void 위시리스트_저장_성공() {
        Wish wish = new Wish(member, product1);

        Wish savedWish = wishRepository.save(wish);

        assertThat(savedWish).isNotNull();
        assertThat(savedWish.getId()).isNotNull();
        assertThat(savedWish.getMember()).isEqualTo(member);
        assertThat(savedWish.getProduct()).isEqualTo(product1);
        assertThat(savedWish.getCreatedDate()).isNotNull();
    }

    @Test
    void 멤버로_위시리스트_조회_성공() {
        Wish wish1 = new Wish(member, product1);
        Wish wish2 = new Wish(member, product2);
        entityManager.persist(wish1);
        entityManager.persist(wish2);
        entityManager.flush();

        Page<Wish> wishlist = wishRepository.findByMember(member, PageRequest.of(0, 5));

        assertThat(wishlist.getContent())
                .hasSize(2)
                .contains(wish1, wish2);
    }

    @Test
    void 위시_중복_저장_실패() {
        Wish wish1 = new Wish(member, product1);
        entityManager.persist(wish1);
        entityManager.flush();

        Wish wish2 = new Wish(member, product1);
        assertThatThrownBy(() -> {
            wishRepository.saveAndFlush(wish2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 멤버와_상품으로_위시_조회_성공() {
        Wish wish = new Wish(member, product1);
        entityManager.persist(wish);
        entityManager.flush();

        Optional<Wish> foundWish = wishRepository.findByMemberAndProduct(member, product1);

        assertThat(foundWish).isPresent();
        assertThat(foundWish.get()).isEqualTo(wish);
    }
}
