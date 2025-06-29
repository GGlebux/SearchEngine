package engine.dto.responses;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorResponse {
    private final static boolean result = false;
    private final String error;
}
