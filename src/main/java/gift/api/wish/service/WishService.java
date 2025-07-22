package gift.api.wish.service;

import gift.api.member.domain.Member;
import gift.api.member.repository.MemberRepository;
import gift.api.product.domain.Product;
import gift.api.product.repository.ProductRepository;
import gift.api.wish.domain.Wish;
import gift.api.wish.dto.WishResponseDto;
import gift.api.wish.repository.WishRepository;
import gift.exception.auth.AuthorizationException;
import gift.exception.conflict.WishDuplicateException;
import gift.exception.notfound.MemberNotFoundException;
import gift.exception.notfound.ProductNotFoundException;
import gift.exception.notfound.WishNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WishService {

    private final WishRepository wishRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public WishService(WishRepository wishRepository,
            MemberRepository memberRepository,
            ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    public Page<WishResponseDto> getWishlist(String email, Pageable pageable) {
        Member member = findMemberByEmailOrThrow(email);

        Page<Wish> wishlistPage = wishRepository.findByMember(member, pageable);

        return wishlistPage.map(wish -> WishResponseDto.of(wish, wish.getProduct()));
    }

    @Transactional
    public WishResponseDto addProductToWishlist(String email, Long productId) {
        Member member = findMemberByEmailOrThrow(email);

        Product product = findProductByIdOrThrow(productId);

        Wish wish = member.addWish(product);

        return WishResponseDto.of(wish, product);
    }

    @Transactional
    public void removeProductFromWishlist(String email, Long wishId) {
        Member member = findMemberByEmailOrThrow(email);

        member.removeWish(wishId);
    }

    private Member findMemberByEmailOrThrow(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    private Product findProductByIdOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
