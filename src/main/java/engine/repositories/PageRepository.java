package engine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import engine.models.Page;
import engine.models.Site;

import java.util.Collection;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    void deleteAllBySiteIn(Collection<Site> domain);
}
