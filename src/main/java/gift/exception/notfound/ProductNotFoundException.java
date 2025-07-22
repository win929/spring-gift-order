package gift.exception.notfound;

public class ProductNotFoundException extends EntityNotFoundException {

    public ProductNotFoundException(Long id) {
        super("해당 ID의 상품을 찾을 수 없습니다: " + id);
    }
}
