package engine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import engine.models.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Modifying
    @Query(value = "INSERT INTO lemma (site_id, lemma, frequency) " +
            "VALUES (:siteId, :lemma, :initialFrequency) " +
            "ON CONFLICT ON CONSTRAINT uk_lemma_site " +
            "DO UPDATE SET frequency = lemma.frequency + :incrementValue",
            nativeQuery = true)
    void upsertLemma(
            @Param("siteId") int siteId,
            @Param("lemma") String lemma,
            @Param("initialFrequency") long initialFrequency,
            @Param("incrementValue") long incrementValue
    );
}
