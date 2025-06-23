package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteUrl;
import searchengine.config.SitesList;
import searchengine.models.Site;
import searchengine.models.Status;
import searchengine.repositories.SiteRepository;

import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static java.util.Set.of;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static searchengine.models.Status.INDEXING;

@Service
@Transactional(readOnly = true)
public class SiteService {
    private final SiteRepository repo;
    private final SitesList targetSites;
    private final VisitedLinksService visitedLinksService;

    @Autowired
    public SiteService(SiteRepository repo, SitesList targetSites, VisitedLinksService visitedLinksService) {
        this.repo = repo;
        this.targetSites = targetSites;
        this.visitedLinksService = visitedLinksService;
    }

    @Transactional
    public synchronized void updateSiteStatus(Site site, Status status, Optional<String> error) {
        site.setStatus(status);
        site.setStatusTime(now());
        error.ifPresent(site::setLastError);
        repo.save(site);
    }

    @Transactional
    public void deleteAllByUrlIn(List<String> urls) {
        repo.deleteAllByUrlIn(urls);
    }

    @Transactional
    public List<Site> saveAll(List<Site> sites) {
        return repo.saveAll(sites);
    }

    @Transactional
    public List<Site> getPreparedConfigSites() {
        List<String> sitesToDelete = targetSites
                .getSiteUrls()
                .stream()
                .map(SiteUrl::getUrl)
                .toList();

        visitedLinksService.clearVisited(sitesToDelete);
        this.deleteAllByUrlIn(sitesToDelete);

        List<Site> sitesToSave =
                targetSites
                        .getSiteUrls()
                        .stream()
                        .map(siteUrl -> Site
                                .builder()
                                .url(siteUrl.getUrl())
                                .name(siteUrl.getName())
                                .status(INDEXING)
                                .statusTime(now())
                                .lastError(EMPTY)
                                .lemmas(of())
                                .pages(of())
                                .build())
                        .toList();

        return this.saveAll(sitesToSave);
    }
}
