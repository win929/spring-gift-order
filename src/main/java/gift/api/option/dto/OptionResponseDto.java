package gift.api.option.dto;

import gift.api.option.domain.Option;

public record OptionResponseDto(
        Long id,
        String name,
        int quantity
) {

    public static OptionResponseDto from(Option option) {
        return new OptionResponseDto(
                option.getId(),
                option.getName(),
                option.getQuantity()
        );
    }
}
