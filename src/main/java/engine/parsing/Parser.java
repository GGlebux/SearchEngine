package engine.parsing;

import lombok.AllArgsConstructor;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import engine.exceptions.ParsingException;
import engine.services.VisitedLinksService;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static java.util.List.of;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static org.jsoup.Jsoup.connect;

@AllArgsConstructor
public class Parser {
    private final VisitedLinksService visitedLinks;
    private final String domainUrl;
    private final static List<String> AGENTS;
    private final static Map<String, String> HEADERS;
    private final static Random RANDOM = new Random();
    private final static List<String> REFEREES;
    private final static WebDriver driver;
    private final static WebDriverWait wait;

    static {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        AGENTS = of("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36)");
        HEADERS = Map.of(
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                "Accept-Language", "en-US,en;q=0.5",
                "Referer", "https://www.google.com/"
        );

        REFEREES = List.of(
                "https://www.google.com/",            // Google (основной)
                "https://www.google.com/search?q=example",     // Google Search
                "https://yandex.ru/",                          // Яндекс
                "https://yandex.ru/search/?text=example",      // Яндекс Поиск
                "https://www.bing.com/",                       // Bing
                "https://duckduckgo.com/",                     // DuckDuckGo
                "https://www.reddit.com/",                     // Reddit (часто в рефералах)
                "https://twitter.com/",                        // Twitter
                "https://www.facebook.com/",                   // Facebook
                "https://t.me/",                               // Telegram
                "https://news.ycombinator.com/",               // Hacker News
                "https://medium.com/",                         // Medium
                "https://github.com/",                         // GitHub
                ""                                             // Пустой (прямой заход)
        );
    }

    public PageData parseUrl(String pagePath) throws ParsingException {
        String address = domainUrl + pagePath;

        Response response;
        Document doc;
        try {
            response = connect(address)
                    .userAgent(getRandomElemFrom(AGENTS))
                    .referrer(getRandomElemFrom(REFEREES))
                    .headers(HEADERS)
                    .execute();

            int statusCode = response.statusCode();
            if (statusCode >= 400) {
                String errorMessage = getHttpErrorMessage(statusCode);
                throw new ParsingException(errorMessage, pagePath, statusCode);
            }

            driver.get(address);
            WebElement openPopupButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".open-popup-btn")
            ));
            openPopupButton.click();

            WebElement popup = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".modal-content")
            ));

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 500)");
            WebElement dynamicContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".lazy-loaded-text")
            ));

            String pageText = driver.findElement(By.tagName("body")).getText();
            pageText += popup.getText();
            pageText += dynamicContent.getText();
//            driver.quit();

            doc = response.parse();
            if (isNull(doc)) {
                throw new ParsingException("Не удалось распознать содержание страницы", pagePath, 0);
            }

            Set<String> links = extractLinks(doc);
            System.out.println(doc.html());
            return new PageData(pagePath, response.statusCode(), pageText, links);

        } catch (MalformedURLException e) {
            throw new ParsingException("Некорректный URL адрес: " + address, pagePath, 404);
        } catch (SocketTimeoutException e) {
            throw new ParsingException("Превышено время ожидания ответа от сервера", pagePath, 0);
        } catch (UnknownHostException e) {
            throw new ParsingException("Не удалось найти указанный сайт: " + e.getMessage(), pagePath, 404);
        } catch (HttpStatusException e) {
            String errorMessage = getHttpErrorMessage(e.getStatusCode());
            throw new ParsingException(errorMessage, pagePath, e.getStatusCode());
        } catch (IOException e) {
            throw new ParsingException("Ошибка при загрузке страницы: " + e.getMessage(), pagePath, 0);
        } catch (Exception e) {
            throw new ParsingException("Неожиданная ошибка при обработке страницы: " + e.getMessage(), pagePath, 0);
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

    private String getRandomElemFrom(List<String> elems) {
        return elems
                .stream()
                .skip(RANDOM.nextInt(elems.size()))
                .findFirst().get();
    }
}
