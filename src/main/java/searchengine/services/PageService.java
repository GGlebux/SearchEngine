package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.models.Page;
import searchengine.models.Site;
import searchengine.repositories.PageRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PageService {
    private final PageRepository repo;

    @Autowired
    public PageService(PageRepository siteRepo) {
        this.repo = siteRepo;
    }

    public synchronized boolean existBySiteAndPath(Site site, String path) {
        return repo.existsBySiteAndPath(site, path);
    }

    @Transactional
    public synchronized void save(Page page) {
       repo.save(page);
    }

    @Transactional
    public synchronized void savePagesBatch(List<Page> batch){
        repo.saveAll(batch);
        batch.clear();
    }
}
