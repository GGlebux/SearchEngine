package searchengine;

import java.util.Set;

public interface Parser {
    String getSiteTitle(String url);
    Set<String> parseUrl(String url);
}
