package gift;

import static org.assertj.core.api.Assertions.assertThat;

import gift.api.member.domain.MemberRole;
import gift.api.member.dto.MemberRequestDto;
import gift.api.member.dto.TokenResponseDto;
import gift.api.product.dto.ProductResponseDto;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaginationE2ETest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private JdbcClient jdbcClient;

    private String userAuthToken;
    private String adminAuthToken;
    private Long memberId;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        String userEmail = "user@test.com";
        String password = "password";
        jdbcClient.sql(
                        "INSERT INTO member(email, password, role) VALUES (:email, :password, :role)")
                .param("email", userEmail)
                .param("password", BCrypt.hashpw(password, BCrypt.gensalt()))
                .param("role", MemberRole.USER.name())
                .update();

        TokenResponseDto userTokenResponse = restClient.post()
                .uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MemberRequestDto(userEmail, password))
                .retrieve()
                .body(TokenResponseDto.class);
        this.userAuthToken = Objects.requireNonNull(userTokenResponse).token();
        this.memberId = jdbcClient.sql("SELECT id FROM member WHERE email = :email")
                .param("email", userEmail)
                .query(Long.class)
                .single();

        String adminEmail = "admin@test.com";
        jdbcClient.sql(
                        "INSERT INTO member(email, password, role) VALUES (:email, :password, :role)")
                .param("email", adminEmail)
                .param("password", BCrypt.hashpw(password, BCrypt.gensalt()))
                .param("role", MemberRole.ADMIN.name())
                .update();

        TokenResponseDto adminTokenResponse = restClient.post()
                .uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MemberRequestDto(adminEmail, password))
                .retrieve()
                .body(TokenResponseDto.class);
        this.adminAuthToken = Objects.requireNonNull(adminTokenResponse).token();

        for (int i = 1; i <= 10; i++) {
            jdbcClient.sql(
                            "INSERT INTO product(name, price, image_url) VALUES (:name, :price, :image_url)")
                    .param("name", "Test Product " + i)
                    .param("price", 1000L * i)
                    .param("image_url", "http://test.com/product" + i + ".jpg")
                    .update();

            if (i <= 5) {
                Long productId = jdbcClient.sql("SELECT id FROM product WHERE name = :name")
                        .param("name", "Test Product " + i)
                        .query(Long.class)
                        .single();
                jdbcClient.sql(
                                "INSERT INTO wish(member_id, product_id, created_date) VALUES (:member_id, :product_id, NOW())")
                        .param("member_id", memberId)
                        .param("product_id", productId)
                        .update();
            }
        }
    }

    @AfterEach
    void tearDown() {
        jdbcClient.sql("delete from wish").update();
        jdbcClient.sql("delete from product").update();
        jdbcClient.sql("delete from member").update();
    }

    @Test
    @DisplayName("관리자 상품 목록 페이지네이션 테스트")
    void adminProductPaginationTest() {
        // 첫 페이지 (기본값: size=5, sort=id,asc)
        List<ProductResponseDto> firstPage = restClient.get()
                .uri("/api/products?page=0")
                .header("Authorization", adminAuthToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductResponseDto>>() {
                });

        assertThat(firstPage).hasSize(5);
        assertThat(firstPage.get(0).name()).isEqualTo("Test Product 1");

        // 두 번째 페이지
        List<ProductResponseDto> secondPage = restClient.get()
                .uri("/api/products?page=1")
                .header("Authorization", adminAuthToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductResponseDto>>() {
                });

        assertThat(secondPage).hasSize(5);
        assertThat(secondPage.get(0).name()).isEqualTo("Test Product 6");
    }

    @Test
    @DisplayName("사용자 상품 목록 페이지네이션 및 정렬 테스트")
    void userProductPaginationAndSortTest() {
        // 가격 내림차순 정렬 및 페이지네이션
        List<ProductResponseDto> sortedByPrice = restClient.get()
                .uri("/api/products?page=0&size=3&sort=price,desc")
                .header("Authorization", userAuthToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductResponseDto>>() {
                });

        assertThat(sortedByPrice).hasSize(3);
        assertThat(sortedByPrice.get(0).name()).isEqualTo("Test Product 10");
        assertThat(sortedByPrice.get(0).price()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("위시리스트 페이지네이션 테스트")
    void wishListPaginationTest() {
        // 첫 페이지 (기본값: size=5, sort=created_date,desc)
        List<?> firstPage = restClient.get()
                .uri("/api/wishes?page=0&size=3")
                .header("Authorization", userAuthToken)
                .retrieve()
                .body(List.class);

        assertThat(firstPage).hasSize(3);

        // 두 번째 페이지
        List<?> secondPage = restClient.get()
                .uri("/api/wishes?page=1&size=3")
                .header("Authorization", userAuthToken)
                .retrieve()
                .body(List.class);

        assertThat(secondPage).hasSize(2);
    }
}