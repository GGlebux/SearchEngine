package engine.exceptions;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ParsingException extends Exception {
    private final String path;
    private final Integer errorCode;

    public ParsingException(String message, String path,Integer errorCode) {
        super(message);
        this.path = path;
        this.errorCode = errorCode;
    }
}
