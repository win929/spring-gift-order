package gift.api.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gift.api.option.domain.Option;
import gift.api.option.dto.OptionRequestDto;
import gift.api.option.dto.OptionResponseDto;
import gift.api.option.repository.OptionRepository;
import gift.api.product.domain.Product;
import gift.api.product.dto.ProductRequestDto;
import gift.api.product.dto.ProductResponseDto;
import gift.api.product.repository.ProductRepository;
import gift.exception.conflict.OptionNameDuplicateException;
import gift.exception.notfound.OptionNotFoundException;
import gift.exception.notfound.ProductNotFoundException;
import gift.exception.option.InvalidOptionAccessException;
import gift.exception.option.OptionPolicyException;
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
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OptionRepository optionRepository;

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
        Product newProduct = new Product(requestDto.name(), requestDto.price(),
                requestDto.imageUrl());
        ReflectionTestUtils.setField(newProduct, "id", 1L); // ID 설정

        given(productRepository.save(any(Product.class))).willReturn(newProduct);

        // when
        ProductResponseDto response = productService.createProduct(requestDto);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("새 상품");
        // 기본 옵션이 저장되는지 확인
        verify(optionRepository, times(1)).save(any());
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
        assertThat(product.getName()).isEqualTo("수정된 상품"); // 원본 객체가 변경되었는지 확인
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_success() {
        // given
        given(productRepository.existsById(1L)).willReturn(true);

        // when
        productService.deleteProduct(1L);

        // then
        // deleteById가 1L을 인자로 호출되었는지 검증
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
        OptionRequestDto requestDto = new OptionRequestDto("새 옵션", 10);
        Option newOption = new Option(requestDto.name(), requestDto.quantity(), product);
        ReflectionTestUtils.setField(newOption, "id", 2L);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findByProductAndName(product, "새 옵션")).willReturn(Optional.empty());
        given(optionRepository.save(any(Option.class))).willReturn(newOption);

        // when
        OptionResponseDto response = productService.addOption(1L, requestDto);

        // then
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.name()).isEqualTo("새 옵션");
        verify(optionRepository).save(any(Option.class));
    }

    @Test
    @DisplayName("옵션 추가 실패 - 중복된 이름")
    void addOption_fail_duplicateName() {
        // given
        OptionRequestDto requestDto = new OptionRequestDto("기존 옵션", 10);
        Option existingOption = new Option("기존 옵션", 5, product);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findByProductAndName(product, "기존 옵션")).willReturn(
                Optional.of(existingOption));

        // when & then
        assertThatThrownBy(() -> productService.addOption(1L, requestDto))
                .isInstanceOf(OptionNameDuplicateException.class);
    }

    @Test
    @DisplayName("옵션 수정 성공")
    void updateOption_success() {
        // given
        Option option = new Option("원본 옵션", 5, product);
        ReflectionTestUtils.setField(option, "id", 10L);
        OptionRequestDto requestDto = new OptionRequestDto("수정된 옵션", 20);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findById(10L)).willReturn(Optional.of(option));
        given(optionRepository.findByProductAndName(product, "수정된 옵션")).willReturn(
                Optional.empty());

        // when
        OptionResponseDto response = productService.updateOption(1L, 10L, requestDto);

        // then
        assertThat(response.name()).isEqualTo("수정된 옵션");
        assertThat(response.quantity()).isEqualTo(20);
        assertThat(option.getName()).isEqualTo("수정된 옵션");
    }

    @Test
    @DisplayName("옵션 수정 실패 - 다른 상품의 옵션 수정 시도")
    void updateOption_fail_invalidAccess() {
        // given
        Product otherProduct = new Product("다른 상품", 100L, "other.jpg");
        ReflectionTestUtils.setField(otherProduct, "id", 2L);

        Option option = new Option("옵션", 5, otherProduct); // 다른 상품에 속한 옵션
        ReflectionTestUtils.setField(option, "id", 10L);

        OptionRequestDto requestDto = new OptionRequestDto("수정 시도", 10);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findById(10L)).willReturn(Optional.of(option));

        // when & then
        assertThatThrownBy(() -> productService.updateOption(1L, 10L, requestDto))
                .isInstanceOf(InvalidOptionAccessException.class)
                .hasMessage("해당 상품에 속한 옵션이 아닙니다.");
    }


    @Test
    @DisplayName("옵션 삭제 성공")
    void deleteOption_success() {
        // given
        Option option1 = new Option("옵션 1", 1, product);
        ReflectionTestUtils.setField(option1, "id", 10L);
        Option option2 = new Option("옵션 2", 1, product);
        ReflectionTestUtils.setField(option2, "id", 11L);
        product.getOptions().add(option1);
        product.getOptions().add(option2);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findById(10L)).willReturn(Optional.of(option1));

        // when
        productService.deleteOption(1L, 10L);

        // then
        // product.getOptions() 리스트에서 option1이 제거되었는지 상태를 검증
        assertThat(product.getOptions()).hasSize(1).contains(option2);
        assertThat(product.getOptions()).doesNotContain(option1);
    }

    @Test
    @DisplayName("옵션 삭제 실패 - 상품에 옵션이 하나뿐인 경우")
    void deleteOption_fail_lastOption() {
        // given
        Option lastOption = new Option("마지막 옵션", 1, product);
        ReflectionTestUtils.setField(lastOption, "id", 10L);
        product.getOptions().add(lastOption);

        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findById(10L)).willReturn(Optional.of(lastOption));

        // when & then
        assertThatThrownBy(() -> productService.deleteOption(1L, 10L))
                .isInstanceOf(OptionPolicyException.class)
                .hasMessage("상품에는 최소 1개의 옵션이 존재해야 합니다.");

        // delete 메소드가 호출되지 않았는지 검증
        verify(optionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("옵션 삭제 실패 - 존재하지 않는 옵션")
    void deleteOption_fail_notFound() {
        // given
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(optionRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.deleteOption(1L, 99L))
                .isInstanceOf(OptionNotFoundException.class);
    }
}