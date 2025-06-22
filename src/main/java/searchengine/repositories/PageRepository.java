package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.models.Page;
import searchengine.models.Site;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    boolean existsBySiteAndPath(Site domain, String path);
}
