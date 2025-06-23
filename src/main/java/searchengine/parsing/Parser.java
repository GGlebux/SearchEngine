package searchengine.parsing;

import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.exceptions.ParsingException;
import searchengine.services.VisitedLinksService;

import java.io.IOException;
import java.util.Set;

import static java.lang.System.err;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class Parser {
    private final VisitedLinksService visitedLinks;
    private final String domainUrl;
    private final static String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
    private final static String REFERER = "https://www.google.com/";

    private final static String NOT_FOUND = "Страница не найдена (неправильная ссылка или страница удалена)";

    public PageData parseUrl(String pagePath) throws ParsingException {
        String address = domainUrl + pagePath;

        Connection.Response response;
        Document doc;
        try {
            response = Jsoup.connect(address)
                    .userAgent(AGENT)
                    .referrer(REFERER)
                    .timeout(10000)
                    .execute();
            doc = response.parse();

            Set<String> links = extractLinks(doc, pagePath);

            return new PageData(pagePath, response.statusCode(), doc.body().text(), links);

        } catch (HttpStatusException e) {
            throw new ParsingException(NOT_FOUND, e.getStatusCode());
        } catch (IOException e) {
            err.printf("Parsing error: '%s' in page '%s' by '%s'\n", e.getMessage(), address, e.getCause());
            throw new RuntimeException();
        }
    }

    private Set<String> extractLinks(Document doc, String pagePath) {
        return doc
                .select("a[href]")
                .stream()
                .map(elem -> elem.attr("href"))
                .filter(l -> l.startsWith(domainUrl) || l.startsWith("/"))      // относительная или абсолютная доменная ссылка
                .filter(l -> !l.contains("#"))                                  // исключаем якорные ссылки
                .map(l -> l.contains("?") ? l.substring(0, l.indexOf("?")) : l) // очищаем ссылки от параметров
                .map(l -> (l.startsWith(domainUrl)) ? l.replace(domainUrl, "") : l) // переводим всё к относительным ссылкам
                .map(l -> l.isEmpty() || l.equals("/") ? "/" : l)               // нормализуем корневую страницу
                .filter(l -> !visitedLinks.contains(domainUrl, l))              // удаляем уже посещенные
                .filter(l -> l.matches("(?i).*\\.(html?|php|aspx?|jsp)(\\?.*)?$")
                        || !l.contains(".")
                        || l.matches(".+/[^.?]+(\\?.*)?$"))               // исключаем ссылки на ресурсы
                .collect(toSet());
    }
}
