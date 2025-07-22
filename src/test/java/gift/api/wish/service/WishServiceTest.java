package gift.api.wish.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import gift.api.member.domain.Member;
import gift.api.member.domain.MemberRole;
import gift.api.member.repository.MemberRepository;
import gift.api.product.domain.Product;
import gift.api.product.repository.ProductRepository;
import gift.api.wish.domain.Wish;
import gift.api.wish.dto.WishResponseDto;
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
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    // WishRepository는 더 이상 서비스에서 직접 사용되지 않으므로 Mock 객체가 필요 없습니다.

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        member = new Member("user@test.com", "password", MemberRole.USER);
        ReflectionTestUtils.setField(member, "id", 1L);

        product = new Product("테스트 상품", 10000L, "test.jpg");
        ReflectionTestUtils.setField(product, "id", 101L);
    }

    @Test
    @DisplayName("위시리스트 추가 성공")
    void addProductToWishlist_success() {
        // given: 서비스가 실제로 호출하는 Repository의 동작을 정의합니다.
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        given(productRepository.findById(101L)).willReturn(Optional.of(product));

        // when: 서비스를 실행합니다.
        WishResponseDto response = wishService.addProductToWishlist("user@test.com", 101L);

        // then: 결과를 검증합니다.
        // 1. DTO의 ID는 DB 저장 전이므로 null이어야 합니다.
        assertThat(response.id()).isNull();
        // 2. DTO의 상품 정보는 올바르게 담겨야 합니다.
        assertThat(response.product().id()).isEqualTo(101L);
        // 3. Member 객체의 내부 wishList 상태가 올바르게 변경되었는지 확인합니다.
        assertThat(member.getWishList()).hasSize(1);
        assertThat(member.getWishList().getFirst().getProduct()).isEqualTo(product);
    }

    @Test
    @DisplayName("위시리스트 추가 실패 - 이미 추가된 상품")
    void addProductToWishlist_fail_duplicate() {
        // given: '이미 추가된' 상황을 만들기 위해 member 객체에 미리 wish를 추가합니다.
        member.addWish(product);

        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        given(productRepository.findById(101L)).willReturn(Optional.of(product));

        // when & then: member.addWish() 내부의 중복 검사 로직이 예외를 발생시키는지 확인합니다.
        assertThatThrownBy(() -> wishService.addProductToWishlist("user@test.com", 101L))
                .isInstanceOf(WishDuplicateException.class);
    }

    @Test
    @DisplayName("위시리스트 삭제 성공")
    void removeProductFromWishlist_success() {
        // given: '삭제할 대상이 있는' 상황을 만듭니다.
        Wish wishToRemove = member.addWish(product);
        ReflectionTestUtils.setField(wishToRemove, "id", 1001L);
        assertThat(member.getWishList()).hasSize(1); // 전제 조건 확인

        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));

        // when
        wishService.removeProductFromWishlist("user@test.com", 1001L);

        // then: Member 객체의 내부 wishList가 비었는지 확인합니다.
        assertThat(member.getWishList()).isEmpty();
    }

    @Test
    @DisplayName("위시리스트 삭제 실패 - 존재하지 않는 위시 ID")
    void removeProductFromWishlist_fail_notFound() {
        // given: Member는 찾을 수 있지만, 그 Member의 wishList는 비어있는 상황입니다.
        given(memberRepository.findByEmail("user@test.com")).willReturn(Optional.of(member));
        assertThat(member.getWishList()).isEmpty(); // 전제 조건 확인

        // when & then: member.removeWish() 내부에서 wishId를 찾지 못해 예외가 발생하는지 확인합니다.
        assertThatThrownBy(() -> wishService.removeProductFromWishlist("user@test.com", 9999L))
                .isInstanceOf(WishNotFoundException.class);
    }

    @Test
    @DisplayName("다른 사용자의 위시 삭제 시도 실패 (실제 동작에 따른 WishNotFoundException 검증)")
    void removeProductFromWishlist_fail_unauthorized() {
        // given:
        // 1. 삭제 대상인 wish(ID: 1001L)는 'member'가 소유하고 있습니다.
        Wish targetWish = member.addWish(product);
        ReflectionTestUtils.setField(targetWish, "id", 1001L);

        // 2. 삭제를 시도하는 사용자는 'otherMember'입니다.
        Member otherMember = new Member("other@test.com", "password", MemberRole.USER);
        ReflectionTestUtils.setField(otherMember, "id", 2L);
        given(memberRepository.findByEmail("other@test.com")).willReturn(Optional.of(otherMember));

        // when & then:
        // 'otherMember'는 자신의 wishList에서 1001L을 찾으려 하지만, 당연히 없습니다.
        // 따라서 현재 로직에서는 소유권 검사(Authorization) 이전에 '찾을 수 없음(NotFound)' 예외가 발생합니다.
        // 테스트는 이 실제 동작을 그대로 검증해야 합니다.
        assertThatThrownBy(() -> wishService.removeProductFromWishlist("other@test.com", 1001L))
                .isInstanceOf(WishNotFoundException.class);
    }
}