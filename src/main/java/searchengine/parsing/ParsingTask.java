package searchengine.parsing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import searchengine.exceptions.ParsingException;
import searchengine.models.Page;
import searchengine.models.Site;
import searchengine.models.Status;
import searchengine.services.PageService;
import searchengine.services.SiteService;
import searchengine.services.VisitedLinksService;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.err;
import static java.lang.Thread.currentThread;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static searchengine.models.Status.*;
import static searchengine.services.IndexingServiceImpl.ROOT_PATH;

@AllArgsConstructor
public class ParsingTask extends RecursiveAction {
    private final Site domain;
    private final String root;
    private final Parser parser;
    private final PageService pageService;
    private final SiteService siteService;
    private final VisitedLinksService visitedLinks;
    @Getter
    private final AtomicReference<Throwable> error = new AtomicReference<>();
    private final static EnumSet<Status> statuses = EnumSet.of(INDEXED, INDEXING);

    @Override
    protected void compute() {
        try {
            if (currentThread().isInterrupted()) {
                return;
            }

            if (visitedLinks.contains(domain.getUrl(), root)) {
                return;
            }
            visitedLinks.add(domain.getUrl(), root);

            PageData data = parser.parseUrl(root);
            pageService.save(new Page(domain, data.getPath(), data.getCode(), data.getContent()));
            siteService.updateSiteStatus(domain, INDEXING, empty(), statuses);

            Set<ParsingTask> subtask = data
                    .getLinks()
                    .stream()
                    .map(link -> new ParsingTask(domain, link, parser, pageService, siteService, visitedLinks))
                    .collect(toSet());

            SECONDS.sleep(1);

            if (!currentThread().isInterrupted()) {
                invokeAll(subtask);
            }

        } catch (InterruptedException e) {
            currentThread().interrupt();
            err.println("Поток прерван=" + currentThread().getName());
        } catch (ParsingException e) {
            String message = e.getMessage();
            pageService.save(new Page(domain, e.getPath(), e.getErrorCode(), ""));
            if (root.equals(ROOT_PATH)) {
                siteService.updateSiteStatus(domain, FAILED, of(message), statuses);
            } else {
                siteService.updateSiteStatus(domain, INDEXING, of(message), statuses);
            }
            err.println(message);
        }
    }
}
