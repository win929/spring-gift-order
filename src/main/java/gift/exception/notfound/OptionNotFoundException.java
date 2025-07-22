package gift.exception.notfound;

public class OptionNotFoundException extends EntityNotFoundException {

    public OptionNotFoundException(Long id) {
        super("해당 ID의 옵션을 찾을 수 없습니다: " + id);
    }
}
