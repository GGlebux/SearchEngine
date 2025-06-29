package engine.services;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import engine.models.Site;
import engine.parsing.Parser;
import engine.parsing.ParsingTask;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static java.lang.System.out;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.SECONDS;
import static engine.models.Status.*;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SiteService siteService;
    private final PageService pageService;
    private final VisitedLinksService visitedLinksService;
    private final List<CompletableFuture<Void>> futures;
    private final ObjectProvider<ForkJoinPool> provider;
    private volatile ForkJoinPool forkJoinPool;
    public static final String ROOT_PATH;

    static {
        ROOT_PATH = "/";
    }

    @Autowired
    public IndexingServiceImpl(SiteService siteService, PageService pageService, VisitedLinksService visitedLinksService,
                               ObjectProvider<ForkJoinPool> provider) {
        this.siteService = siteService;
        this.pageService = pageService;
        this.visitedLinksService = visitedLinksService;
        this.provider = provider;
        this.forkJoinPool = provider.getObject();
        this.futures = new ArrayList<>();
    }

    @Override
    public void startIndexing() {
        this.stopIndexing();

        List<Site> sitesToIndex = siteService.getPreparedConfigSites();

        for (Site site : sitesToIndex) {
            futures.add(runAsync(() -> indexingSite(site)));
        }

        allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public void stopIndexing() {
        futures.forEach(f -> f.cancel(true));
        futures.clear();

        forkJoinPool.shutdownNow();

        try {
            // Ждём 1 секунду, чтобы задачи успели завершиться
            if (!forkJoinPool.awaitTermination(1, SECONDS)) {
                forkJoinPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        forkJoinPool = provider.getObject();

        for (Site failedSite : siteService.findTargetSites()) {
            siteService.updateSiteStatus(failedSite, FAILED,
                    of("Индексация остановлена пользователем"), EnumSet.of(INDEXING));
        }
    }


    private void indexingSite(Site site) {
        Parser parser = new Parser(visitedLinksService, site.getUrl());
        ParsingTask parsingTask =
                new ParsingTask(site, ROOT_PATH, parser, pageService, siteService, visitedLinksService);
        forkJoinPool.invoke(parsingTask);
        out.printf("Parsed site with url: '%s'\n", site.getUrl());
        siteService.updateSiteStatus(site, INDEXED, empty(), EnumSet.of(INDEXING));
    }
}
