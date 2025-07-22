package gift.api.wish.dto;

import jakarta.validation.constraints.NotNull;

public record WishRequestDto(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId
) {

}
