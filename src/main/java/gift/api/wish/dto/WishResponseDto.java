package gift.api.wish.dto;

import gift.api.product.domain.Product;
import gift.api.wish.domain.Wish;
import gift.api.product.dto.ProductResponseDto;

public record WishResponseDto(
        Long id,
        ProductResponseDto product
) {

    public static WishResponseDto of(Wish wish, Product product) {
        return new WishResponseDto(
                wish.getId(),
                ProductResponseDto.from(product)

        );
    }
}
