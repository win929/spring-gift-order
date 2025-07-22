package gift.exception.conflict;

public class EmailDuplicateException extends DataConflictException {

    public EmailDuplicateException(String email) {
        super("이미 존재하는 이메일입니다: " + email);
    }
}
