package gift.api.option.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gift.api.option.domain.Option;
import gift.api.product.domain.Product;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // 실제 DB 설정 유지
class OptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OptionRepository optionRepository;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product("상품 A", 1000L, "a.jpg");
        product2 = new Product("상품 B", 2000L, "b.jpg");

        entityManager.persist(product1);
        entityManager.persist(product2);
    }

    @Test
    @DisplayName("상품 ID로 옵션 목록 조회 성공")
    void findByProductId_success() {
        // given
        Option optionA1 = new Option("기본", 10, product1);
        Option optionA2 = new Option("사이즈업", 5, product1);
        Option optionB1 = new Option("기본", 20, product2);

        entityManager.persist(optionA1);
        entityManager.persist(optionA2);
        entityManager.persist(optionB1);
        entityManager.flush();

        // when
        List<Option> options = optionRepository.findByProductId(product1.getId());

        // then
        assertThat(options).hasSize(2);
        assertThat(options).extracting(Option::getName).containsExactlyInAnyOrder("기본", "사이즈업");
    }

    @Test
    @DisplayName("상품과 이름으로 옵션 조회 성공")
    void findByProductAndName_success() {
        // given
        Option option = new Option("검색용 옵션", 15, product1);
        entityManager.persist(option);
        entityManager.flush();

        // when
        Optional<Option> foundOption = optionRepository.findByProductAndName(product1, "검색용 옵션");

        // then
        assertThat(foundOption).isPresent();
        assertThat(foundOption.get().getName()).isEqualTo("검색용 옵션");
        assertThat(foundOption.get().getProduct()).isEqualTo(product1);
    }

    @Test
    @DisplayName("옵션 저장 실패 - 한 상품 내에서 옵션 이름 중복")
    void save_fail_duplicateNameInProduct() {
        // given
        Option option1 = new Option("중복된 이름", 10, product1);
        entityManager.persist(option1);
        entityManager.flush();

        // when & then
        Option option2 = new Option("중복된 이름", 20, product1);

        // UniqueConstraint(columnNames = {"product_id", "name"}) 에 의해 예외 발생
        assertThatThrownBy(() -> optionRepository.saveAndFlush(option2)).isInstanceOf(
                DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("옵션 저장 성공 - 다른 상품 간에는 이름 중복 허용")
    void save_success_duplicateNameAcrossProducts() {
        // given
        Option optionA = new Option("공통 옵션", 10, product1);
        Option optionB = new Option("공통 옵션", 20, product2);

        // when
        optionRepository.save(optionA);
        optionRepository.save(optionB);
        entityManager.flush();

        // then
        Optional<Option> foundA = optionRepository.findByProductAndName(product1, "공통 옵션");
        Optional<Option> foundB = optionRepository.findByProductAndName(product2, "공통 옵션");

        assertThat(foundA).isPresent();
        assertThat(foundB).isPresent();
        assertThat(foundA.get()).isNotEqualTo(foundB.get());
    }
}