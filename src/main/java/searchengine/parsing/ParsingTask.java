package searchengine.parsing;

import lombok.AllArgsConstructor;
import searchengine.models.Page;
import searchengine.models.Site;
import searchengine.services.PageService;
import searchengine.services.SiteService;
import searchengine.services.VisitedLinksService;

import java.util.Set;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.currentThread;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;
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
        if (visitedLinks.contains(domain.getUrl(), root)) {
            return;
        }
        visitedLinks.add(domain.getUrl(), root);

        PageData data = parser.parseUrl(root);
        pageService.save(new Page(domain, data.getPath(), data.getCode(), data.getContent()));
        siteService.updateSiteStatus(domain, INDEXING, of(""));

        Set<ParsingTask> subtask = data
                .getLinks()
                .stream()
                .map(link -> new ParsingTask(domain, link, parser, pageService, siteService, visitedLinks))
                .collect(toSet());

        try {
            MILLISECONDS.sleep(500);
        } catch (InterruptedException _) {
            System.err.println("Поток прерван=" + currentThread().getName());
        }

        invokeAll(subtask);
    }
}
