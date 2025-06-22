package searchengine.parsing;

import lombok.AllArgsConstructor;
import searchengine.models.Page;
import searchengine.models.Site;
import searchengine.services.PageService;
import searchengine.services.SiteService;

import java.util.Set;
import java.util.concurrent.RecursiveAction;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;
import static searchengine.models.Status.INDEXING;

@AllArgsConstructor
public class ParseTask extends RecursiveAction {
    private final Site domain;
    private final String root;
    private final Parser parser;
    private final PageService pageService;
    private final SiteService siteService;

    @Override
    protected void compute() {

        if (pageService.existBySiteAndPath(domain, root)) {
            return;
        }

        PageData data = parser.parseUrl(root);

        pageService.save(new Page(domain, data.getPath(), data.getCode(), data.getContent()));
        siteService.updateSiteStatus(domain, INDEXING, of(""));


        Set<ParseTask> subtask = data
                .getLinks()
                .stream()
                .map(link -> new ParseTask(domain, link, parser, pageService, siteService))
                .collect(toSet());

        invokeAll(subtask);
    }
}
