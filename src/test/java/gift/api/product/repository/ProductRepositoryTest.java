package gift.api.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import gift.api.product.domain.Product;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void 상품_저장_및_조회_성공() {
        Product product = new Product(
                "Test Product",
                4500L,
                "test.jpg"
        );

        Product savedProduct = productRepository.save(product);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo(savedProduct.getName());
        assertThat(foundProduct.get().getPrice()).isEqualTo(savedProduct.getPrice());
        assertThat(foundProduct.get().getImageUrl()).isEqualTo(savedProduct.getImageUrl());
    }

    @Test
    void 상품_수정_성공() {
        Product product = new Product(
                "Original Product",
                5000L,
                "original.jpg"
        );
        entityManager.persistAndFlush(product);

        Product updatedProduct = productRepository.findById(product.getId()).orElse(null);
        assertThat(updatedProduct).isNotNull();

        updatedProduct.update(
                "Updated Product",
                6000L,
                "updated.jpg"
        );
        productRepository.saveAndFlush(updatedProduct);

        Product updatedFoundProduct = productRepository.findById(product.getId()).orElse(null);

        assertThat(updatedFoundProduct).isNotNull();
        assertThat(updatedFoundProduct.getName()).isEqualTo("Updated Product");
        assertThat(updatedFoundProduct.getPrice()).isEqualTo(6000L);
        assertThat(updatedFoundProduct.getImageUrl()).isEqualTo("updated.jpg");
    }

    @Test
    void 상품_삭제_성공() {
        Product product = new Product(
                "Test Product",
                100L,
                "test.jpg"
        );
        entityManager.persistAndFlush(product);

        assertThat(productRepository.findById(product.getId())).isPresent();

        productRepository.deleteById(product.getId());
        productRepository.flush();

        assertThat(productRepository.findById(product.getId())).isNotPresent();
    }
}