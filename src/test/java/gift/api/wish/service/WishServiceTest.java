package gift.api.wish.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import gift.api.member.domain.Member;
import gift.api.member.domain.MemberRole;
import gift.api.member.repository.MemberRepository;
import gift.api.product.domain.Product;
import gift.api.product.repository.ProductRepository;
import gift.api.wish.domain.Wish;
import gift.api.wish.dto.WishResponseDto;
import gift.api.wish.repository.WishRepository;
import gift.exception.auth.AuthorizationException;
import gift.exception.conflict.WishDuplicateException;
import gift.exception.notfound.WishNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WishServiceTest {

    @InjectMocks
    private WishService wishService;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    private Member member;
    private Member otherMember;
    private Product product;
    private Wish wish;

    @BeforeEach
    void setUp() {
        member = new Member("user@test.com", "password", MemberRole.USER);
        ReflectionTestUtils.setField(member, "id", 1L);

        otherMember = new Member("other@test.com", "password", MemberRole.USER);
        ReflectionTestUtils.setField(otherMember, "id", 2L);

        product = new Product("테스트 상품", 10000L, "test.jpg");
        ReflectionTestUtils.setField(product, "id", 101L);

        wish = new Wish(member, product);
        ReflectionTestUtils.setField(wish, "id", 1001L);
    }

    @Test
    @DisplayName("위시리스트 추가 성공")
    void addProductToWishlist_success() {
        // given
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        given(productRepository.findById(101L)).willReturn(Optional.of(product));
        given(wishRepository.findByMemberAndProduct(member, product)).willReturn(Optional.empty());
        given(wishRepository.save(any(Wish.class))).willReturn(wish);

        // when
        WishResponseDto response = wishService.addProductToWishlist("user@test.com", 101L);

        // then
        assertThat(response.product().id()).isEqualTo(101L);
        assertThat(response.id()).isEqualTo(1001L);
        verify(wishRepository).save(any(Wish.class));
    }

    @Test
    @DisplayName("위시리스트 추가 실패 - 이미 추가된 상품")
    void addProductToWishlist_fail_duplicate() {
        // given
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        given(productRepository.findById(101L)).willReturn(Optional.of(product));
        given(wishRepository.findByMemberAndProduct(member, product)).willReturn(Optional.of(wish));

        // when & then
        assertThatThrownBy(() -> wishService.addProductToWishlist("user@test.com", 101L))
                .isInstanceOf(WishDuplicateException.class);
    }

    @Test
    @DisplayName("위시리스트 삭제 성공")
    void removeProductFromWishlist_success() {
        // given
        given(wishRepository.findById(1001L)).willReturn(Optional.of(wish));

        // when
        wishService.removeProductFromWishlist("user@test.com", 1001L);

        // then
        verify(wishRepository).delete(wish);
    }

    @Test
    @DisplayName("위시리스트 삭제 실패 - 존재하지 않는 위시 ID")
    void removeProductFromWishlist_fail_notFound() {
        // given
        given(wishRepository.findById(9999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishService.removeProductFromWishlist("user@test.com", 9999L))
                .isInstanceOf(WishNotFoundException.class);
    }

    @Test
    @DisplayName("위시리스트 삭제 실패 - 권한 없음")
    void removeProductFromWishlist_fail_unauthorized() {
        // given
        given(wishRepository.findById(1001L)).willReturn(Optional.of(wish)); // wish는 'member' 소유

        // when & then
        // 'otherMember'가 삭제 시도
        assertThatThrownBy(() -> wishService.removeProductFromWishlist("other@test.com", 1001L))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("해당 위시를 삭제할 권한이 없습니다.");
    }
}