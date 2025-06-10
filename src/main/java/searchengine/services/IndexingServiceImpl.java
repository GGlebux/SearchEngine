package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.Parser;
import searchengine.models.Site;
import searchengine.repositories.SiteRepository;

import static java.time.Instant.now;
import static searchengine.models.Status.INDEXING;

@Service
@Transactional(readOnly = true)
public class IndexingServiceImpl implements IndexingService {
    private final SiteRepository siteRepo;
    private final Parser parser;

    @Autowired
    public IndexingServiceImpl(SiteRepository siteRepo, Parser parser) {
        this.siteRepo = siteRepo;
        this.parser = parser;
    }


    @Override
    @Transactional
    public void startIndexing(String url) {
        siteRepo.deleteByUrl(url);
        Site site = Site.builder()
                .name(parser.getSiteTitle(url))
                .status(INDEXING)
                .statusTime(now())
                .lastError("")
                .build();
        siteRepo.save(site);
        parser.parseUrl(url);
    }
}
