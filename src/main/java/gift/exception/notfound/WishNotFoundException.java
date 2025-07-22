package gift.exception.notfound;

public class WishNotFoundException extends EntityNotFoundException {

    public WishNotFoundException(Long id) {
        super("해당 ID의 위시를 찾을 수 없습니다: " + id);
    }
}
