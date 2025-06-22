package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteUrl;
import searchengine.config.SitesList;
import searchengine.models.Status;
import searchengine.models.Site;
import searchengine.repositories.SiteRepository;

import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static searchengine.models.Status.INDEXING;

@Service
@Transactional(readOnly = true)
public class SiteService {
    private final SiteRepository repo;
    private final SitesList targetSites;

    @Autowired
    public SiteService(SiteRepository repo, SitesList targetSites) {
        this.repo = repo;
        this.targetSites = targetSites;
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
        this.deleteAllByUrlIn(targetSites
                .getSiteUrls()
                .stream()
                .map(SiteUrl::getUrl)
                .toList());

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
                                .build())
                        .toList();

        return this.saveAll(sitesToSave);
    }
}
