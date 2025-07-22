package gift.exception.conflict;

public class WishDuplicateException extends DataConflictException {

    public WishDuplicateException(String productName) {
        super("이미 위시리스트에 추가된 상품입니다: " + productName);
    }
}
