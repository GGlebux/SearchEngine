package engine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import engine.models.Site;
import engine.models.Status;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

@Repository
public interface SiteRepository extends JpaRepository<Site, Integer> {
    Set<Site> findAllByUrlIn(Collection<String> urls);

    @Modifying
    @Query("""
            UPDATE Site s SET s.status = :status,
            s.lastError = CASE WHEN :error IS NOT NULL THEN :error ELSE s.lastError END,
            s.statusTime = :time WHERE s.id = :id AND s.status IN :statusesToUpdate""")
    void updateSelectedStates(@Param("id") Integer id,
                              @Param("status") Status status,
                              @Param("error") String error,
                              @Param("time") Instant time,
                              @Param("statusesToUpdate") EnumSet<Status> statusesToUpdate);
}
