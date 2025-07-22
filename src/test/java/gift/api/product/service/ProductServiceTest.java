package gift.api.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gift.api.option.domain.Option;
import gift.api.option.dto.OptionRequestDto;
import gift.api.option.dto.OptionResponseDto;
import gift.api.product.domain.Product;
import gift.api.product.dto.ProductRequestDto;
import gift.api.product.dto.ProductResponseDto;
import gift.api.product.repository.ProductRepository;
import gift.exception.conflict.OptionNameDuplicateException;
import gift.exception.notfound.OptionNotFoundException;
import gift.exception.notfound.ProductNotFoundException;
import gift.exception.option.OptionPolicyException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("테스트 상품", 10000L, "test.jpg");
        ReflectionTestUtils.setField(product, "id", 1L);
    }

    @Test
    @DisplayName("상품 생성 성공 - 기본 옵션과 함께")
    void createProduct_success() {
        // given
        ProductRequestDto requestDto = new ProductRequestDto("새 상품", 15000L, "new.jpg");
        Product fakeSavedProduct = new Product(requestDto.name(), requestDto.price(),
                requestDto.imageUrl());
        ReflectionTestUtils.setField(fakeSavedProduct, "id", 1L);

        given(productRepository.save(any(Product.class))).willReturn(fakeSavedProduct);

        // when
        ProductResponseDto response = productService.createProduct(requestDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("새 상품");

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertThat(capturedProduct.getOptions()).hasSize(1);
        assertThat(capturedProduct.getOptions().getFirst().getName()).isEqualTo("기본 옵션");
    }

    @Test
    @DisplayName("상품 ID로 조회 성공")
    void findProductById_success() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        ProductResponseDto response = productService.findProductById(1L);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 조회 실패")
    void findProductById_fail_notFound() {
        // given
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.findProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("해당 ID의 상품을 찾을 수 없습니다: 99");
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_success() {
        // given
        ProductRequestDto requestDto = new ProductRequestDto("수정된 상품", 12000L, "updated.jpg");
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        ProductResponseDto response = productService.updateProduct(1L, requestDto);

        // then
        assertThat(response.name()).isEqualTo("수정된 상품");
        assertThat(response.price()).isEqualTo(12000L);
        assertThat(product.getName()).isEqualTo("수정된 상품");
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_success() {
        // given
        given(productRepository.existsById(1L)).willReturn(true);

        // when
        productService.deleteProduct(1L);

        // then
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 실패")
    void deleteProduct_fail_notFound() {
        // given
        given(productRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // --- 옵션 관련 테스트 ---

    @Test
    @DisplayName("옵션 추가 성공")
    void addOption_success() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        OptionRequestDto requestDto = new OptionRequestDto("새 옵션", 10);

        // when
        OptionResponseDto response = productService.addOption(1L, requestDto);

        // then
        assertThat(response.name()).isEqualTo("새 옵션");
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(product.getOptions()).hasSize(1);
        assertThat(product.getOptions().getFirst().getName()).isEqualTo("새 옵션");
    }

    @Test
    @DisplayName("옵션 추가 실패 - 중복된 이름")
    void addOption_fail_duplicateName() {
        // given
        product.addOption("기존 옵션", 5);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        OptionRequestDto requestDto = new OptionRequestDto("기존 옵션", 10);

        // when & then
        assertThatThrownBy(() -> productService.addOption(1L, requestDto))
                .isInstanceOf(OptionNameDuplicateException.class);
    }

    @Test
    @DisplayName("옵션 수정 성공")
    void updateOption_success() {
        // given
        Option option = product.addOption("원본 옵션", 5);
        // ⭐ ID를 수동으로 설정해줍니다.
        ReflectionTestUtils.setField(option, "id", 10L);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        OptionRequestDto requestDto = new OptionRequestDto("수정된 옵션", 20);

        // when
        productService.updateOption(1L, 10L, requestDto);

        // then
        assertThat(product.getOptions().getFirst().getName()).isEqualTo("수정된 옵션");
        assertThat(product.getOptions().getFirst().getQuantity()).isEqualTo(20);
    }

    @Test
    @DisplayName("옵션 삭제 성공")
    void deleteOption_success() {
        // given
        Option option1 = product.addOption("옵션 1", 1);
        ReflectionTestUtils.setField(option1, "id", 10L); // ⭐ ID 설정
        Option option2 = product.addOption("옵션 2", 1);
        ReflectionTestUtils.setField(option2, "id", 11L); // ⭐ ID 설정

        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        productService.deleteOption(1L, 10L); // ID 10L 삭제

        // then
        assertThat(product.getOptions()).hasSize(1);
        assertThat(product.getOptions().getFirst().getName()).isEqualTo("옵션 2");
    }

    @Test
    @DisplayName("옵션 삭제 실패 - 상품에 옵션이 하나뿐인 경우")
    void deleteOption_fail_lastOption() {
        // given
        Option lastOption = product.addOption("마지막 옵션", 1);
        ReflectionTestUtils.setField(lastOption, "id", 10L); // ⭐ ID 설정

        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.deleteOption(1L, 10L))
                .isInstanceOf(OptionPolicyException.class);
    }

    @Test
    @DisplayName("옵션 삭제 실패 - 존재하지 않는 옵션")
    void deleteOption_fail_notFound() {
        // given
        // 💡 정책 예외(size<=1)를 피하기 위해 옵션을 2개 이상으로 만듭니다.
        Option option1 = product.addOption("옵션 1", 1);
        ReflectionTestUtils.setField(option1, "id", 10L);
        Option option2 = product.addOption("옵션 2", 1);
        ReflectionTestUtils.setField(option2, "id", 11L);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when & then
        // 이제 NullPointerException이나 OptionPolicyException 없이
        // 우리가 의도한 OptionNotFoundException이 발생하는지 정확하게 검증할 수 있습니다.
        assertThatThrownBy(() -> productService.deleteOption(1L, 999L)) // 존재하지 않는 ID
                .isInstanceOf(OptionNotFoundException.class);
    }
}