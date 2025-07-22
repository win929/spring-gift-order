package gift.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RequiresApprovalWordsValidator implements
        ConstraintValidator<RequiresApprovalWords, String> {

    private List<String> restrictedWords;

    @Override
    public void initialize(RequiresApprovalWords annotation) {
        this.restrictedWords = Arrays.asList(annotation.words());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        List<String> violatedWords = restrictedWords.stream()
                .filter(value::contains)
                .collect(Collectors.toList());

        if (!violatedWords.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "담당 MD의 승인이 필요한 단어가 포함되어 있습니다: " + String.join(", ", violatedWords)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
