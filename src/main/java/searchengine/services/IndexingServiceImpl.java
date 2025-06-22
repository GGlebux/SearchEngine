package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import searchengine.models.Site;
import searchengine.parsing.ParseTask;
import searchengine.parsing.Parser;

import java.util.concurrent.ForkJoinPool;

@Service
public class IndexingServiceImpl implements IndexingService {
    private final SiteService siteService;
    private final PageService pageService;
    private final ThreadPoolTaskExecutor taskExecutor;
    private static final String ROOT_PATH = "/";

    @Autowired
    public IndexingServiceImpl(SiteService siteService, PageService pageService, ThreadPoolTaskExecutor taskExecutor) {
        this.siteService = siteService;
        this.pageService = pageService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void startIndexing() {
        for (Site site : siteService.getPreparedConfigSites()){
            taskExecutor.execute(() -> indexingSite(site));
        }
    }

    private void indexingSite(Site site) {
        try (ForkJoinPool pool = new ForkJoinPool()) {
            Parser parser = new Parser(site.getUrl());
            ParseTask parseTask = new ParseTask(site, ROOT_PATH, parser, pageService, siteService);
            pool.execute(parseTask);
        }
    }
}
