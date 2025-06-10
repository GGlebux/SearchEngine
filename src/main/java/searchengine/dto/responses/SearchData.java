package searchengine.dto.responses;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchData {
    private final String site;
    private final String siteName;
    private final String uri;
    private final String title;
    private final String snipped;
    private final double relevance;
}
