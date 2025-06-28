package searchengine.exceptions;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ParsingException extends RuntimeException {
    private final Integer errorCode;

    public ParsingException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
