package gift.exception.notfound;

public class MemberNotFoundException extends EntityNotFoundException {

    public MemberNotFoundException(String email) {
        super("해당 이메일의 사용자를 찾을 수 없습니다: " + email);
    }
}
