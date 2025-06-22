package searchengine.parsing;

import lombok.AllArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Set;

import static java.lang.System.err;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class Parser {
    private final String domainUrl;
    private final static String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    public PageData parseUrl(String pagePath) {

        String address = domainUrl + pagePath;

        Connection.Response response;
        Document doc;

        try {
            response = Jsoup.connect(address).userAgent(AGENT).execute();
            doc = response.parse();

            Set<String> links = extractLinks(doc, pagePath);
            System.err.printf("Parsed '%s' and find %d links, symbols=%d\n", address, links.size(), doc.body().text().length());
            return new PageData(pagePath, response.statusCode(), doc.body().text(), links);
        } catch (HttpStatusException e) {
            err.printf("Ошибка парсинга страницы '%s', код '%s'%n\n", e.getUrl(), e.getStatusCode());
            throw new RuntimeException();
        } catch (IOException e) {
            err.printf("Parsing error: '%s' in page '%s'\n", e.getMessage(), address);
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
                .map(l -> l.startsWith(domainUrl) ? l.replace(domainUrl, "") : l) // переводим всё к относительным ссылкам
                .map(l -> l.endsWith("/") ? l.substring(0, l.length() - 1) : l) // убираем последний слэш
                .filter(l -> !pagePath.equals(l))                               // не исходная страница
                .collect(toSet());
    }
}
