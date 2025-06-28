package searchengine.parsing;

import lombok.AllArgsConstructor;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.exceptions.ParsingException;
import searchengine.services.VisitedLinksService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class Parser {
    private final VisitedLinksService visitedLinks;
    private final String domainUrl;
    private final static String AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    public PageData parseUrl(String pagePath) throws ParsingException {
        String address = domainUrl + pagePath;

        Response response;
        Document doc;
        try {
            response = Jsoup.connect(address)
                    .userAgent(AGENT)
                    .timeout(10000)
                    .ignoreContentType(true)
                    .ignoreContentType(true)
                    .execute();

            int statusCode = response.statusCode();
            if (statusCode >= 400) {
                String errorMessage = getHttpErrorMessage(statusCode);
                throw new ParsingException(errorMessage, statusCode);
            }

            doc = response.parse();
            if (isNull(doc)) {
                throw new ParsingException("Не удалось распознать содержание страницы", 0);
            }

            Set<String> links = extractLinks(doc);
            return new PageData(pagePath, response.statusCode(), doc.body().text(), links);

        } catch (MalformedURLException e) {
            throw new ParsingException("Некорректный URL адрес: " + address, 0);
        } catch (SocketTimeoutException e) {
            throw new ParsingException("Превышено время ожидания ответа от сервера", 0);
        } catch (UnknownHostException e) {
            throw new ParsingException("Не удалось найти указанный сайт: " + e.getMessage(), 0);
        } catch (HttpStatusException e) {
            String errorMessage = getHttpErrorMessage(e.getStatusCode());
            throw new ParsingException(errorMessage, e.getStatusCode());
        } catch (IOException e) {
            throw new ParsingException("Ошибка при загрузке страницы: " + e.getMessage(), 0);
        } catch (Exception e) {
            throw new ParsingException("Неожиданная ошибка при обработке страницы", 0);
        }
    }

    private Set<String> extractLinks(Document doc) {
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

    private String getHttpErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Неверный запрос к серверу";
            case 401 -> "Требуется авторизация";
            case 403 -> "Доступ запрещен";
            case 404 -> "Страница не найдена";
            case 408 -> "Время ожидания запроса истекло";
            case 429 -> "Слишком много запросов";
            case 500 -> "Ошибка сервера";
            case 502 -> "Плохой шлюз";
            case 503 -> "Сервис недоступен";
            case 504 -> "Время ожидания шлюза истекло";
            default -> "Ошибка HTTP: " + statusCode;
        };
    }
}
