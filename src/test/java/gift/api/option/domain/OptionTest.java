package gift.api.option.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import gift.exception.option.InvalidOptionQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OptionTest {

    @Test
    @DisplayName("재고 차감 성공")
    void subtractQuantity_success() {
        // given
        Option option = new Option("테스트 옵션", 10, null); // Product는 null이어도 무방

        // when
        option.subtractQuantity(3);

        // then
        assertThat(option.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고 차감 실패 - 재고 부족")
    void subtractQuantity_fail_insufficientStock() {
        // given
        Option option = new Option("테스트 옵션", 5, null);

        // when & then
        assertThatThrownBy(() -> option.subtractQuantity(10))
                .isInstanceOf(InvalidOptionQuantityException.class)
                .hasMessage("재고가 부족합니다.");

        // 재고가 부족할 때 기존 수량이 그대로 유지되는지도 확인
        assertThat(option.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("재고 차감 실패 - 모든 재고 소진")
    void subtractQuantity_fail_exactStock() {
        // given
        Option option = new Option("테스트 옵션", 5, null);

        // when
        option.subtractQuantity(5);

        // then
        assertThat(option.getQuantity()).isEqualTo(0);
    }
}