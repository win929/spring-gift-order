package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import gift.api.member.dto.MemberRequestDto;
import gift.api.member.dto.TokenResponseDto;
import gift.api.option.dto.OptionRequestDto;
import gift.api.option.dto.OptionResponseDto;
import gift.api.product.dto.ProductRequestDto;
import gift.api.product.dto.ProductResponseDto;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductOptionE2ETest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private JdbcClient jdbcClient;

    private String authToken;
    private Long productId;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        // 회원 가입 및 토큰 발급
        MemberRequestDto memberDto = new MemberRequestDto("option_test@test.com", "password");
        TokenResponseDto tokenResponse = restClient.post()
                .uri("/api/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(memberDto)
                .retrieve()
                .body(TokenResponseDto.class);
        this.authToken = Objects.requireNonNull(tokenResponse).token();

        // 테스트용 상품 생성
        ProductRequestDto productDto = new ProductRequestDto("옵션 테스트 상품", 1000L, "image.jpg");
        ProductResponseDto productResponse = restClient.post()
                .uri("/api/products")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(productDto)
                .retrieve()
                .body(ProductResponseDto.class);
        this.productId = Objects.requireNonNull(productResponse).id();
    }

    @AfterEach
    void tearDown() {
        jdbcClient.sql("delete from wish").update();
        jdbcClient.sql("delete from option").update();
        jdbcClient.sql("delete from product").update();
        jdbcClient.sql("delete from member").update();
    }

    @Test
    @DisplayName("상품의 모든 옵션 조회")
    void getOptionsForProductTest() {
        // when
        List<OptionResponseDto> options = restClient.get()
                .uri("/api/products/{productId}/options", productId)
                .header("Authorization", authToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OptionResponseDto>>() {
                });

        // then
        assertThat(options).isNotNull();
        assertThat(options).hasSize(1); // 기본 옵션
        assertThat(options.getFirst().name()).isEqualTo("기본 옵션");
    }

    @Test
    @DisplayName("상품에 새 옵션 추가")
    void addOptionToProductTest() {
        // given
        OptionRequestDto requestDto = new OptionRequestDto("추가 옵션", 10);

        // when
        OptionResponseDto response = restClient.post()
                .uri("/api/products/{productId}/options", productId)
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .body(OptionResponseDto.class);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("추가 옵션");
        assertThat(response.quantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("옵션 정보 수정")
    void updateOptionTest() {
        // given
        // 먼저 수정할 옵션을 추가합니다.
        OptionRequestDto addDto = new OptionRequestDto("수정 전 옵션", 5);
        OptionResponseDto addedOption = restClient.post()
                .uri("/api/products/{productId}/options", productId)
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(addDto)
                .retrieve()
                .body(OptionResponseDto.class);

        OptionRequestDto updateDto = new OptionRequestDto("수정 후 옵션", 99);

        // when
        OptionResponseDto updatedResponse = restClient.put()
                .uri("/api/products/{productId}/options/{optionId}", productId, addedOption.id())
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateDto)
                .retrieve()
                .body(OptionResponseDto.class);

        // then
        assertThat(updatedResponse.name()).isEqualTo("수정 후 옵션");
        assertThat(updatedResponse.quantity()).isEqualTo(99);
    }

    @Test
    @DisplayName("옵션 삭제")
    void deleteOptionTest() {
        // given
        // 삭제할 옵션을 추가합니다.
        OptionRequestDto addDto = new OptionRequestDto("삭제될 옵션", 1);
        OptionResponseDto addedOption = restClient.post()
                .uri("/api/products/{productId}/options", productId)
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(addDto)
                .retrieve()
                .body(OptionResponseDto.class);

        // when
        restClient.delete()
                .uri("/api/products/{productId}/options/{optionId}", productId, addedOption.id())
                .header("Authorization", authToken)
                .retrieve()
                .toBodilessEntity();

        // then
        List<OptionResponseDto> options = restClient.get()
                .uri("/api/products/{productId}/options", productId)
                .header("Authorization", authToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OptionResponseDto>>() {
                });

        assertThat(options).hasSize(1); // 기본 옵션만 남아야 합니다.
        assertThat(options.getFirst().name()).isEqualTo("기본 옵션");
    }

    @Test
    @DisplayName("옵션 삭제 실패 - 마지막 옵션")
    void deleteLastOptionFailTest() {
        // given
        // 상품 생성 시 기본 옵션이 하나만 있는 상태
        Long optionId = jdbcClient.sql("SELECT id FROM option WHERE product_id = :productId")
                .param("productId", productId)
                .query(Long.class)
                .single();

        // when
        Throwable thrown = catchThrowable(() -> restClient.delete()
                .uri("/api/products/{productId}/options/{optionId}", productId, optionId)
                .header("Authorization", authToken)
                .retrieve()
                .toBodilessEntity());

        // then
        assertThat(thrown)
                .isNotNull() // 예외가 발생했는지 확인
                .isInstanceOf(HttpClientErrorException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT)
                .hasMessageContaining("상품에는 최소 1개의 옵션이 존재해야 합니다.");
    }
}