package engine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import engine.models.Page;
import engine.repositories.PageRepository;

@Service
@Transactional(readOnly = true)
public class PageService {
    private final PageRepository repo;

    @Autowired
    public PageService(PageRepository siteRepo) {
        this.repo = siteRepo;
    }

    @Transactional
    public void save(Page page) {
        repo.insertOnConflict(page.getSite().getId(),
                page.getPath(),
                page.getCode(),
                page.getContent());
    }
}
