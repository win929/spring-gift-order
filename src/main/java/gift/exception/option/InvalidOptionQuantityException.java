package gift.exception.option;

import gift.exception.conflict.DataConflictException;

public class InvalidOptionQuantityException extends DataConflictException {

    public InvalidOptionQuantityException(String message) {
        super(message);
    }
}
