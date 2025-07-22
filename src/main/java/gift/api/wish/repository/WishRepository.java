package gift.api.wish.repository;

import gift.api.member.domain.Member;
import gift.api.product.domain.Product;
import gift.api.wish.domain.Wish;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishRepository extends JpaRepository<Wish, Long> {

    Page<Wish> findByMember(Member member, Pageable pageable);

    Optional<Wish> findByMemberAndProduct(Member member, Product product);
}
