package searchengine.parsing;

import lombok.AllArgsConstructor;
import searchengine.models.Page;
import searchengine.models.Site;
import searchengine.services.PageService;
import searchengine.services.SiteService;
import searchengine.services.VisitedLinksService;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.currentThread;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static searchengine.models.Status.INDEXED;
import static searchengine.models.Status.INDEXING;

@AllArgsConstructor
public class ParsingTask extends RecursiveAction {
    private final Site domain;
    private final String root;
    private final Parser parser;
    private final PageService pageService;
    private final SiteService siteService;
    private final VisitedLinksService visitedLinks;

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
            siteService.updateSiteStatus(domain, INDEXING, of(""), EnumSet.of(INDEXED, INDEXING));

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
            System.err.println("Поток прерван=" + currentThread().getName());
        } catch (Exception _) {
        }
    }
}
