package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SiteUrl;
import searchengine.config.Config;
import searchengine.models.Site;
import searchengine.models.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toSet;
import static searchengine.models.Status.INDEXING;

@Service
@Transactional(readOnly = true)
public class SiteService {
    private final SiteRepository siteRepo;
    private final PageRepository pageRepo;
    private final Config targetSites;
    private final VisitedLinksService visitedLinksService;
    private final Lock updateLock = new ReentrantLock();

    @Autowired
    public SiteService(SiteRepository siteRepo, PageRepository pageRepo, Config targetSites, VisitedLinksService visitedLinksService) {
        this.siteRepo = siteRepo;
        this.pageRepo = pageRepo;
        this.targetSites = targetSites;
        this.visitedLinksService = visitedLinksService;
    }

        @Transactional
        public void updateSiteStatus(Site site, Status status, Optional<String> error, EnumSet<Status> statusesToUpdate) {
            updateLock.lock();
            try {
                siteRepo.updateSelectedStates(site.getId(), status, error.orElse(null), now(), statusesToUpdate);
            } finally {
                updateLock.unlock();
            }
        }

    @Transactional
    public List<Site> getPreparedConfigSites() {
        List<Site> preparedSites = new ArrayList<>();

        Set<String> targetUrls = this.getTargetUrls();

        Set<Site> sitesFromBD = siteRepo.findAllByUrlIn(targetUrls);
        Set<String> urlsFromBD = sitesFromBD
                .stream()
                .map(Site::getUrl)
                .collect(toSet());

        // Очищаем РЕДИС
        visitedLinksService.clearVisited(targetUrls);

        // Создаем несуществующие
        List<Site> newSites = createConfigSites(
                targetSites
                        .getSiteUrls()
                        .stream()
                        .filter(su -> !urlsFromBD.contains(su.getUrl()))
                        .collect(toSet()));
        // Обновляем существующие
        List<Site> oldSites = prepareConfigSites(sitesFromBD
                .stream()
                .filter(s -> urlsFromBD.contains(s.getUrl()))
                .collect(toSet()));

        preparedSites.addAll(newSites);
        preparedSites.addAll(oldSites);

        return preparedSites;
    }

    @Transactional
    public List<Site> createConfigSites(Collection<SiteUrl> targetUrls) {
        return siteRepo.saveAll(targetUrls
                .stream()
                .map(info -> new Site(info.getName(), info.getUrl(), INDEXING, ""))
                .toList());
    }

    @Transactional
    public List<Site> prepareConfigSites(Collection<Site> existedSites) {
        pageRepo.deleteAllBySiteIn(existedSites);
        return siteRepo.saveAll(existedSites
                .stream()
                .peek(s -> s.setStatus(INDEXING))
                .peek(s -> s.setLastError(""))
                .peek(s -> s.setStatusTime(now()))
                .toList());
    }

    @Transactional
    public Set<Site> findTargetSites() {
        return siteRepo.findAllByUrlIn(getTargetUrls());
    }


    private Set<String> getTargetUrls() {
        return targetSites
                .getSiteUrls()
                .stream()
                .map(SiteUrl::getUrl)
                .collect(toSet());
    }
}
