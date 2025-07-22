package gift.api.product.dto;

import gift.validation.RequiresApprovalWords;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductRequestDto(
        @NotBlank(message = "상품 이름은 필수입니다.")
        @Size(max = 15, message = "상품 이름은 최대 15자여야 합니다.")
        @Pattern(
                regexp = "^[a-zA-Z0-9가-힣 ()\\[\\]+\\-&/_]*$",
                message = "상품 이름에는 (), [], +, -, &, /, _ 외의 특수 문자를 사용할 수 없습니다."
        )
        @RequiresApprovalWords
        String name,

        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        Long price,

        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl
) {

}
