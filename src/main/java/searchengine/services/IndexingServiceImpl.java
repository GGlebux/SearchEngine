package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.models.Site;
import searchengine.parsing.Parser;
import searchengine.parsing.ParsingTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static searchengine.models.Status.FAILED;
import static searchengine.models.Status.INDEXED;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SiteService siteService;
    private final PageService pageService;
    private final VisitedLinksService visitedLinksService;
    private static final String ROOT_PATH = "/";
    private static final ForkJoinPool forkJoinPool = commonPool();

    @Autowired
    public IndexingServiceImpl(SiteService siteService, PageService pageService, VisitedLinksService visitedLinksService) {
        this.siteService = siteService;
        this.pageService = pageService;
        this.visitedLinksService = visitedLinksService;
    }

    @Override
    public void startIndexing() {
        List<Site> sitesToIndex = siteService.getPreparedConfigSites();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Site site : sitesToIndex) {
            futures.add(runAsync(() -> indexingSite(site)));
        }
        allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    for (Site site : sitesToIndex) {
                        siteService.updateSiteStatus(site, INDEXED, of(""));
                    }});
    }

    private void indexingSite(Site site) {
        try  {
            Parser parser = new Parser(visitedLinksService, site.getUrl());
            ParsingTask parsingTask = new ParsingTask(site, ROOT_PATH, parser, pageService, siteService, visitedLinksService);

            forkJoinPool.execute(parsingTask);
        } catch (Exception e) {
            siteService.updateSiteStatus(site, FAILED, of(e.getMessage()));
        }
    }
}
