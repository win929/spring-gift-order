package gift.api.option.repository;

import gift.api.option.domain.Option;
import gift.api.product.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Long> {

    List<Option> findByProductId(Long productId);

    Optional<Option> findByProductAndName(Product product, String name);
}
