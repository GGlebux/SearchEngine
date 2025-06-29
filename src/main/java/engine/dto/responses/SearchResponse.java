package engine.dto.responses;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SearchResponse {
    private final boolean result = true;
    private final int count;
    private final List<SearchData> data;
}
