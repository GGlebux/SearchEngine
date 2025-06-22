package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.models.Site;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    void deleteAllByUrlIn(List<String> urls);
}
