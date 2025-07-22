package gift.exception.conflict;

public class OptionNameDuplicateException extends DataConflictException {

    public OptionNameDuplicateException(String name) {
        super("이미 존재하는 옵션 이름입니다: " + name);
    }
}
