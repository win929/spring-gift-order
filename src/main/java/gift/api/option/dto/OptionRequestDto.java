package gift.api.option.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OptionRequestDto(
        @NotBlank(message = "옵션 이름은 필수입니다.")
        @Size(max = 50, message = "옵션 이름은 최대 50자여야 합니다.")
        @Pattern(
                regexp = "^[a-zA-Z0-9가-힣 ()\\[\\]+\\-&/_]*$",
                message = "옵션 이름에는 (), [], +, -, &, /, _ 외의 특수 문자를 사용할 수 없습니다."
        )
        String name,

        @Min(value = 1, message = "옵션 수량은 1개 이상이어야 합니다.")
        @Max(value = 99_999_999, message = "옵션 수량은 1억 개 미만이어야 합니다.")
        int quantity
) {

}
